package itkach.aard2;

import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


class DictionaryFinder {

    private final static String T = "DictionaryFinder";

    private boolean cancelRequested;

    private FilenameFilter fileFilter = new FilenameFilter() {
        public boolean accept(File dir, String filename) {
            return filename.toLowerCase().endsWith(".slob") || new File(dir, filename).isDirectory();
        }
    };

    private List<File> discover(File location) {
        List<File> result = new ArrayList<File>();

        result.addAll(scanDir(location));

        return result;
    }

    private List<File> scanDir(File dir) {
        if (cancelRequested) {
            return Collections.emptyList();
        }
        String absolutePath = dir.getAbsolutePath();

        Log.d(T, "Scanning " + absolutePath);
        List<File> candidates = new ArrayList<File>();
        File[] files = dir.listFiles(fileFilter);

        if (files != null) {
            for (File file : files) {
                if (cancelRequested) {
                    break;
                }

                Log.d(T, "Considering " + file.getAbsolutePath());
                if (file.isDirectory()) {
                    Log.d(T, "Directory: " + file.getAbsolutePath());
                    candidates.addAll(scanDir(file));
                } else {
                    if (!file.isHidden() && file.isFile()) {
                        Log.d(T, "Candidate: " + file.getAbsolutePath());
                        candidates.add(file);
                    } else {
                        Log.d(T, "Hidden or not a file: " + file.getAbsolutePath());
                    }
                }
            }
        }
        return candidates;
    }

    synchronized List<SlobDescriptor> findDictionaries(File location) {
        cancelRequested = false;
        Log.d(T, "starting dictionary discovery");
        long t0 = System.currentTimeMillis();
        List<File> candidates = discover(location);
        Log.d(T, "dictionary discovery took " + (System.currentTimeMillis() - t0));

        List<SlobDescriptor> descriptors = new ArrayList<SlobDescriptor>();
        Set<String> seen = new HashSet<String>();
        for (File f : candidates) {
            SlobDescriptor sd = SlobDescriptor.fromFile(f);
            if (sd.id != null && seen.contains(sd.id)) {
                continue;
            }
            seen.add(sd.id);
            long currentTime = System.currentTimeMillis();
            sd.createdAt = currentTime;
            sd.lastAccess = currentTime;
            descriptors.add(sd);
        }
        return descriptors;
    }

    void cancel() {
        cancelRequested = true;
    }
}
