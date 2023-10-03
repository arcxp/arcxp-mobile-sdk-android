package com.arcxp.video.players;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;

/**
 * Default implementation of track filter. This implementation only shown track that have a non-null language.
 * Also, this implementation hides the track provided by the server response (ID_SUBTITLE_URL) if the video stream already has an embedded one.
 * @hide
 */
public class DefaultTrackFilter implements ArcTrackSelectionView.TrackFilter {

    @Override
    public boolean filter(Format format, TrackGroupArray trackGroups) {
        if (format == null) return false;
        if (format.language == null) return false;
        if (PostTvPlayerImpl.ID_SUBTITLE_URL.equals(format.id)) {
            // Show ARC captions track if there is nothing else to show
            for (int i = 0; i < trackGroups.length; i++) {
                TrackGroup trackGroup = trackGroups.get(i);
                for (int j = 0; j < trackGroup.length; j++) {
                    Format anotherFormat = trackGroup.getFormat(j);
                    if (anotherFormat != null //TODO note format is non null, so can't test a null format
                            && !PostTvPlayerImpl.ID_SUBTITLE_URL.equals(anotherFormat.id)
                            && anotherFormat.language != null) {
                        return false;
                    }
                }
            }
            return true;
        }
        return true;
    }
}
