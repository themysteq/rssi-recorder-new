package pl.mysteq.software.rssirecordernew.managers;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by mysteq on 2017-04-10.
 */

public class FileManager {
    public static final String LogTAG = "FileManager";
    public static void copyFile(File src, File dst) throws IOException
    {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try
        {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
        finally
        {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }
    private static String convertHashToString(byte[] digestBytes) {
        String returnVal = "";
        for (int i = 0; i < digestBytes.length; i++) {
            returnVal += Integer.toString(( digestBytes[i] & 0xff ) + 0x100, 16).substring(1);
        }
        return returnVal;
    }

    public static String calculateDigest(File input){
        Log.d(LogTAG,String.format("Calculating hash from: %s",input.getName()));
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        try (FileInputStream fis = new FileInputStream(input)) {
            try (BufferedInputStream bis = new BufferedInputStream(fis)) {
                try (DigestInputStream dis = new DigestInputStream(bis, md)) {
  /* Read decorated stream (dis) to EOF as normal... */
                    while (dis.read() != -1) ;

                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert md != null;
        byte[] digest = md.digest();
        return convertHashToString(digest);

    }
    static File copyFileNamedDigest(File folder, String interfix, File srcFile) {
        String extension = srcFile.getName().split("\\.")[1]; //wez rozszerzenie pliku
        //File srcFile = new File(event.getImageFilePath()); //uchwyc plik wejsciowy
        String digest = FileManager.calculateDigest(srcFile); //policz sume kontrolna tresci

        //FIXME: jak to propagowac? Zeby o tym nie zapomniec
        interfix = interfix == null ? "" : interfix;
        String filename = String.format("%s%s.%s", digest, interfix,extension ); //stworz nazwe wg wzoru
        File destFile = new File(folder,filename);

        Log.d(LogTAG,String.format("trying to save file to: %s",destFile.getAbsolutePath()));

        if ( ! destFile.exists()) {
            try {
                FileManager.copyFile(srcFile,destFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else{
            String oldDigest = FileManager.calculateDigest(destFile);
            if(! oldDigest.equals(digest))
                throw new IllegalStateException("Plan file consistency failed!");
            else Log.w(LogTAG,String.format("File %s already exists",destFile.getAbsolutePath()));
        }
        return destFile;
    }

}
