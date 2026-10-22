package com.crosseyes.app.videogen

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecInfo.CodecCapabilities
import android.media.MediaFormat
import android.media.MediaMuxer
import java.nio.ByteBuffer

/**
 * Renders a synthetic H.264 clip (moving gradient) into an mp4 file using
 * a YUV420 flexible pixel path — no Surface required, works on all modern devices.
 */
object VideoGen {

    fun renderVideo(
        width: Int = 720,
        height: Int = 960,
        durationSec: Int = 5,
        fps: Int = 25,
        outPath: String
    ): Boolean {
        val totalFrames = (durationSec * fps).coerceAtLeast(1)
        val yPlane = width * height
        val uvPlane = yPlane / 4
        val frameSize = yPlane + 2 * uvPlane
        val frameDurNs = 1_000_000_000L / fps

        val fmt = MediaFormat.createVideoFormat("video/avc", width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
            setInteger(MediaFormat.KEY_BIT_RATE, 4_500_000)
            setInteger(MediaFormat.KEY_FRAME_RATE, fps)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }

        val encoder = MediaCodec.createEncoderByType("video/avc")
        encoder.configure(fmt, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val muxer = MediaMuxer(outPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var track = -1
        var muxerStarted = false
        var failed = false
        val buffer = ByteBuffer.allocateDirect(frameSize)
        val info = MediaCodec.BufferInfo()

        try {
            // Feed all frames
            for (f in 0 until totalFrames) {
                val inIdx = encoder.dequeueInputBuffer(10_000)
                if (inIdx >= 0) {
                    drain(encoder, muxer, track) { track = 0; muxerStarted = true }
                    writeGradient(buffer, width, height, f)
                    encoder.queueInputBuffer(inIdx, 0, frameSize, f.toLong() * frameDurNs, 0)
                }
            }
            encoder.signalEndOfInputStream()

            // Drain until EOS
            var keep = true
            while (keep) {
                val outIdx = encoder.dequeueOutputBuffer(info, 100_000)
                when {
                    outIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        try {
                            track = muxer.addTrack(encoder.outputFormat)
                            muxer.start()
                            muxerStarted = true
                        } catch (e: Exception) {
                            failed = true; keep = false
                        }
                    }
                    outIdx >= 0 -> {
                        val outBuf = encoder.getOutputBuffer(outIdx)
                        if (outBuf != null && track != -1 && info.size > 0) {
                            outBuf.position(info.offset)
                            outBuf.limit(info.offset + info.size)
                            try {
                                muxer.writeSampleData(track, outBuf, info)
                            } catch (e: Exception) {
                                failed = true; keep = false
                            }
                        }
                        if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) keep = false
                        encoder.releaseOutputBuffer(outIdx, false)
                    }
                    else -> keep = false
                }
            }
        } catch (e: Throwable) {
            failed = true
            e.printStackTrace()
        } finally {
            try { encoder.stop() } catch (_: Exception) {}
            encoder.release()
            if (muxerStarted) {
                try { muxer.stop() } catch (_: Exception) {}
            }
            try { muxer.release() } catch (_: Exception) {}
        }
        return !failed
    }

    private fun drain(encoder: MediaCodec, muxer: MediaMuxer, track: Int, onFormat: () -> Unit) {
        val info = MediaCodec.BufferInfo()
        val outIdx = encoder.dequeueOutputBuffer(info, 0)
        when (outIdx) {
            MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> onFormat()
            else -> {
                if (outIdx >= 0) {
                    val outBuf = encoder.getOutputBuffer(outIdx)
                    if (outBuf != null && track != -1 && info.size > 0) {
                        outBuf.position(info.offset)
                        outBuf.limit(info.offset + info.size)
                        try { muxer.writeSampleData(track, outBuf, info) } catch (_: Exception) {}
                    }
                    encoder.releaseOutputBuffer(outIdx, false)
                }
            }
        }
    }

    private fun writeGradient(buf: ByteBuffer, w: Int, h: Int, frame: Int) {
        val yPlane = w * h
        var i = 0
        while (i < yPlane) {
            val x = i % w
            val y = i / w
            val v = ((x + y + frame) % 256).toByte()
            buf.put(v)
            i++
        }
        var u = 0
        while (u < yPlane / 4) {
            buf.put((120 + (u % 32)).toByte())
            u++
        }
        var v2 = 0
        while (v2 < yPlane / 4) {
            buf.put((128 + (v2 % 32)).toByte())
            v2++
        }
    }
}
