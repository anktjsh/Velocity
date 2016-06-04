/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.util;

import com.gluonhq.charm.down.common.PlatformFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import velocity.core.VelocityCore;

/**
 *
 * @author Aniket
 */
public class FileUtils {

    public static boolean exists(File f) {
        return f.exists();
    }

    public static boolean createDirectories(File f) {
        return f.mkdirs();
    }

    public static boolean createDirectory(File f) {
        return f.mkdir();
    }

    public static boolean createFile(File f) {
        if (f.exists()) {
            return false;
        } else {
            write(f, new ArrayList<>());
            return true;
        }
    }

    public static void write(File f, List<String> al) {
        try (FileWriter fw = new FileWriter(f); PrintWriter out = new PrintWriter(fw)) {
            for (String s : al) {
                out.println(s);
            }
        } catch (IOException ex) {
        }
    }

    public static void copy(InputStream source, OutputStream sink)
            throws IOException {
        byte[] buf = new byte[8192];
        int n;
        while ((n = source.read(buf)) > 0) {
            sink.write(buf, 0, n);
        }
    }

    public static void delete(File f) {
        f.delete();
    }

    public static boolean isDirectory(File f) {
        return f.isDirectory();
    }

    public static boolean isHidden(File f) {
        return f.isHidden();
    }

    public static void move(File a, File b) {
        a.renameTo(b);
    }

    public static byte[] readAllBytes(File f) {
        FileInputStream fileInputStream;
        byte[] bFile = new byte[(int) size(f)];
        try {
            fileInputStream = new FileInputStream(f);
            fileInputStream.read(bFile);
            fileInputStream.close();
        } catch (Exception e) {
        }
        return bFile;
    }

    public static List<String> readAllLines(File f) {
        ArrayList<String> al = new ArrayList<>();
        try {
            Scanner in = new Scanner(f);
            while (in.hasNextLine()) {
                al.add(in.nextLine());
            }
        } catch (IOException e) {
        }
        return al;
    }

    public static long size(File f) {
        return f.length();
    }

    public static File newFile(String s) {
        if (!VelocityCore.isDesktop()) {
            try {
                return new File(PlatformFactory.getPlatform().getPrivateStorage().getAbsolutePath() + File.separator + s);
            } catch (IOException ex) {
            }
        }
        return new File(s);
    }

    public static String probeContentType(File f) {
        String path = f.getAbsolutePath();
        if (path.contains(".")) {
            String extension = path.substring(path.lastIndexOf("."));
            return getMimeTypes().get(extension);
        }
        return null;
    }

    private static TreeMap<String, String> mimeTypes;

    private static TreeMap<String, String> getMimeTypes() {
        if (mimeTypes == null) {
            mimeTypes = new TreeMap<>();
            Scanner in = new Scanner(FileUtils.class.getResourceAsStream("format.txt"));
            while (in.hasNextLine()) {
                String s = in.nextLine();
                String[] spl = s.split("=");
                mimeTypes.put(spl[0], spl[1]);
            }
        }
        return mimeTypes;
    }

    public static boolean isUrl(String input) {
        Matcher m = one.matcher(input);
        if (m.matches()) {
            return true;
        }
        m = two.matcher(input);
        if (m.matches()) {
            return true;
        }
        return input.startsWith("velocityfx://");
    }
    private final static Pattern one = Pattern.compile("(@)?(href=')?(HREF=')?(HREF=\")?(href=\")?(http://)?(https://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?");
    private final static Pattern two = Pattern.compile("((([A-Za-z]{3,9}:(?:\\/\\/)?)(?:[-;:&=\\+\\$,\\w]+@)?[A-Za-z0-9.-]+|(?:www.|[-;:&=\\+\\$,\\w]+@)[A-Za-z0-9.-]+)((?:\\/[\\+~%\\/.\\w-_]*)?\\??(?:[-\\+=&;%@.\\w_]*)#?(?:[\\w]*))?)");

}
