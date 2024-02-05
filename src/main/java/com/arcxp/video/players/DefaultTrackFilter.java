package com.arcxp.video.players;

import com.arcxp.video.views.ArcTrackSelectionView;
import androidx.media3.common.Format;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.source.TrackGroupArray;

/**
 * Default implementation of track filter. This implementation only shown track that have a non-null language.
 * Also, this implementation hides the track provided by the server response (ID_SUBTITLE_URL) if the video stream already has an embedded one.
 * @hide
 */
@UnstableApi
public class DefaultTrackFilter implements ArcTrackSelectionView.TrackFilter {

    static final String ID_SUBTITLE_URL = "ID_SUBTITLE_URL";
    @Override
    public boolean filter(Format format, TrackGroupArray trackGroups) {
        if (format == null) return false;
        if (format.language == null) return false;
        if (ID_SUBTITLE_URL.equals(format.id)) {
            // Show ARC captions track if there is nothing else to show
            for (int i = 0; i < trackGroups.length; i++) {
                TrackGroup trackGroup = trackGroups.get(i);
                for (int j = 0; j < trackGroup.length; j++) {
                    Format anotherFormat = trackGroup.getFormat(j);
                    if (anotherFormat != null //TODO note format is non null, so can't test a null format
                            && !ID_SUBTITLE_URL.equals(anotherFormat.id)
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
