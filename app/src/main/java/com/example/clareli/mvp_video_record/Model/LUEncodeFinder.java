package com.example.clareli.mvp_video_record.Model;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.util.Log;
import android.util.SparseArray;

import com.example.clareli.mvp_video_record.Presenter.LUPresenterCallback;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.clareli.mvp_video_record.Util.IConstant.VIDEO_AVC;

public class LUEncodeFinder {
    private LUPresenterCallback _presenterCallback;
    static SparseArray<String> sAACProfiles = new SparseArray<>();
    static SparseArray<String> sAVCProfiles = new SparseArray<>();
    static SparseArray<String> sAVCLevels = new SparseArray<>();

    static List<MediaCodecInfo> encoderInfos = new ArrayList<>();
    static List<MediaCodecInfo> decoderInfos = new ArrayList<>();


    public LUEncodeFinder(LUPresenterCallback callback) {
        _presenterCallback = callback;
        separateMediaCodecList();
    }

    public MediaCodecInfo[] findEncodersByType(String formatType) {

        if((encoderInfos == null) || (encoderInfos.size() <= 0)) {
            _presenterCallback.errorPreview("Cannot find encoder lists");
            return null;
        }
        List<MediaCodecInfo> infos = new ArrayList<>();
        for(int i =0; i < encoderInfos.size(); i++){
            try {
                MediaCodecInfo.CodecCapabilities cap = encoderInfos.get(i).getCapabilitiesForType(formatType);
                if (cap != null)
                    infos.add(encoderInfos.get(i));
            }catch (IllegalArgumentException e){
                continue;
            }
        }
        return infos.toArray(new MediaCodecInfo[infos.size()]);
    }

    /*
    * 2019-02-11, Clare
    * To separate  Encoder and Decoder */
    public static void separateMediaCodecList(){
        MediaCodecList codecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
        for (MediaCodecInfo info : codecList.getCodecInfos()) {
            if (!info.isEncoder()) {
                decoderInfos.add(info);
            } else {
                encoderInfos.add(info);
            }
        }

    }

    public List<MediaCodecInfo> getEncoderInfos(){
        return encoderInfos;
    }

    public List<MediaCodecInfo> getDecoderInfos(){
        return decoderInfos;
    }

    /**
     * @param avcProfileLevel AVC CodecProfileLevel
     */
    public static String avcProfileLevelToString(MediaCodecInfo.CodecProfileLevel avcProfileLevel) {
        if (sAVCProfiles.size() == 0 || sAVCLevels.size() == 0) {
            initProfileLevels();
        }
        String profile = null, level = null;
        int i = sAVCProfiles.indexOfKey(avcProfileLevel.profile);
        if (i >= 0) {
            profile = sAVCProfiles.valueAt(i);
        }

        i = sAVCLevels.indexOfKey(avcProfileLevel.level);
        if (i >= 0) {
            level = sAVCLevels.valueAt(i);
        }

        if (profile == null) {
            profile = String.valueOf(avcProfileLevel.profile);
        }
        if (level == null) {
            level = String.valueOf(avcProfileLevel.level);
        }
        return profile + '-' + level;
    }

    static String[] aacProfiles() {
        if (sAACProfiles.size() == 0) {
            initProfileLevels();
        }
        String[] profiles = new String[sAACProfiles.size()];
        for (int i = 0; i < sAACProfiles.size(); i++) {
            profiles[i] = sAACProfiles.valueAt(i);
        }
        return profiles;
    }

    public static MediaCodecInfo.CodecProfileLevel toProfileLevel(String str) {
        if (sAVCProfiles.size() == 0 || sAVCLevels.size() == 0 || sAACProfiles.size() == 0) {
            initProfileLevels();
        }
        String profile = str;
        String level = null;
        int i = str.indexOf('-');
        if (i > 0) { // AVC profile has level
            profile = str.substring(0, i);
            level = str.substring(i + 1);
        }

        MediaCodecInfo.CodecProfileLevel res = new MediaCodecInfo.CodecProfileLevel();
        if (profile.startsWith("AVC")) {
            res.profile = keyOfValue(sAVCProfiles, profile);
        } else if (profile.startsWith("AAC")) {
            res.profile = keyOfValue(sAACProfiles, profile);
        } else {
            try {
                res.profile = Integer.parseInt(profile);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        if (level != null) {
            if (level.startsWith("AVC")) {
                res.level = keyOfValue(sAVCLevels, level);
            } else {
                try {
                    res.level = Integer.parseInt(level);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }


        return res.profile > 0 && res.level >= 0 ? res : null;
    }

    public static <T> int keyOfValue(SparseArray<T> array, T value) {
        int size = array.size();
        for (int i = 0; i < size; i++) {
            T t = array.valueAt(i);
            if (t == value || t.equals(value)) {
                return array.keyAt(i);
            }
        }
        return -1;
    }

    public static void initProfileLevels() {
        Field[] fields = MediaCodecInfo.CodecProfileLevel.class.getFields();
        for (Field f : fields) {
            if ((f.getModifiers() & (Modifier.STATIC | Modifier.FINAL)) == 0) {
                continue;
            }
            String name = f.getName();
            SparseArray<String> target;
            if (name.startsWith("AVCProfile")) {
                target = sAVCProfiles;
            } else if (name.startsWith("AVCLevel")) {
                target = sAVCLevels;
            } else if (name.startsWith("AACObject")) {
                target = sAACProfiles;
            } else {
                continue;
            }
            try {
                target.put(f.getInt(null), name);
            } catch (IllegalAccessException e) {
                //ignored
            }
        }
    }


    static SparseArray<String> sColorFormats = new SparseArray<>();

    public static String toGetReadableColorFormat(int colorFormat) {
        if (sColorFormats.size() == 0) {
            initColorFormatFields();
        }
        int i = sColorFormats.indexOfKey(colorFormat);
        if (i >= 0) return sColorFormats.valueAt(i);
        return "0x" + Integer.toHexString(colorFormat);
    }

    static int toColorFormat(String str) {
        if (sColorFormats.size() == 0) {
            initColorFormatFields();
        }
        int color = keyOfValue(sColorFormats, str);
        if (color > 0) return color;
        if (str.startsWith("0x")) {
            return Integer.parseInt(str.substring(2), 16);
        }
        return 0;
    }

    private static void initColorFormatFields() {
        // COLOR_
        Field[] fields = MediaCodecInfo.CodecCapabilities.class.getFields();
        for (Field f : fields) {
            if ((f.getModifiers() & (Modifier.STATIC | Modifier.FINAL)) == 0) {
                continue;
            }
            String name = f.getName();
            if (name.startsWith("COLOR_")) {
                try {
                    int value = f.getInt(null);
                    sColorFormats.put(value, name);
                } catch (IllegalAccessException e) {
                    // ignored
                }
            }
        }

    }

    public static void logCodecInfos(MediaCodecInfo[] codecInfos, String mimeType) {
        for (MediaCodecInfo info : codecInfos) {
            StringBuilder builder = new StringBuilder(512);
            MediaCodecInfo.CodecCapabilities caps = info.getCapabilitiesForType(mimeType);
            builder.append("Encoder '").append(info.getName()).append('\'')
                    .append("\n  supported : ")
                    .append(Arrays.toString(info.getSupportedTypes()));
            MediaCodecInfo.VideoCapabilities videoCaps = caps.getVideoCapabilities();
            if (videoCaps != null) {
                builder.append("\n  Video capabilities:")
                        .append("\n  Widths: ").append(videoCaps.getSupportedWidths())
                        .append("\n  Heights: ").append(videoCaps.getSupportedHeights())
                        .append("\n  Frame Rates: ").append(videoCaps.getSupportedFrameRates())
                        .append("\n  Bitrate: ").append(videoCaps.getBitrateRange());
                if (VIDEO_AVC.equals(mimeType)) {
                    MediaCodecInfo.CodecProfileLevel[] levels = caps.profileLevels;

                    builder.append("\n  Profile-levels: ");
                    for (MediaCodecInfo.CodecProfileLevel level : levels) {
                        builder.append("\n  ").append(avcProfileLevelToString(level));
                    }
                }
                builder.append("\n  Color-formats: ");
                for (int c : caps.colorFormats) {
                    builder.append("\n  ").append(toGetReadableColorFormat(c));
                }
            }
            MediaCodecInfo.AudioCapabilities audioCaps = caps.getAudioCapabilities();
            if (audioCaps != null) {
                builder.append("\n Audio capabilities:")
                        .append("\n Sample Rates: ").append(Arrays.toString(audioCaps.getSupportedSampleRates()))
                        .append("\n Bit Rates: ").append(audioCaps.getBitrateRange())
                        .append("\n Max channels: ").append(audioCaps.getMaxInputChannelCount());
            }
            Log.i("@@@", builder.toString());
        }
    }


}
