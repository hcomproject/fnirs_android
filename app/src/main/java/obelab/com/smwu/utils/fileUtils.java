package obelab.com.smwu.utils;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class fileUtils {
    public static final String STRSAVEPATH = Environment.getExternalStorageDirectory() + "/NIRSIT/SMWU/";
    private static final String TAG = "[fileUtils]";
    public static File TextFile;

    public fileUtils() {

    }

    public static void WriteLog(String TAG, String text) {

        if(TextFile == null || !TextFile.exists()){
            //폴더 생성
            File dir = makeDirectory(STRSAVEPATH);
            SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH");
            //파일 생성
            TextFile = makeFile(dir, (STRSAVEPATH + format.format(new Date(System.currentTimeMillis())))+".txt");
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        writeFile(TextFile, (format.format(new Date(System.currentTimeMillis())) + "  " + TAG + " : " + text + "\n"));
        TextFile = null;
    }

    /**
     * 디렉토리 생성
     *
     * @return dir
     */
    public static File makeDirectory(String dir_path) {
        File dir = new File(dir_path);
        if (!dir.exists()) {
            dir.mkdirs();
            Log.i(TAG, "!dir.exists");
        } else {
            //Log.i(TAG, "dir.exists");
        }

        return dir;
    }

    /**
     * 파일 생성
     *
     * @param dir
     * @return file
     */
    public static File makeFile(File dir, String file_path) {
        File file = null;
        boolean isSuccess = false;
        if (dir.isDirectory()) {
            file = new File(file_path);
            if (file != null && !file.exists()) {
                //Log.i(TAG, "!file.exists");
                try {
                    isSuccess = file.createNewFile();

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i(TAG, "파일생성 여부 = " + file.getAbsolutePath());
                } finally {
                    Log.i(TAG, "파일생성 여부 = " + isSuccess);
                }
            } else {
                //Log.i(TAG, "file.exists");
            }
        }
        return file;
    }

    /**
     * 파일에 내용 쓰기
     *
     * @param file
     * @param file_content
     * @return
     */
    public static boolean writeFile(File file, String file_content) {
        /*boolean result;
        FileOutputStream fos;
        if (file != null && file.exists() && file_content != null) {
            try {
                fos = new FileOutputStream(file);
                try {
                    fos.write(file_content);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            result = true;
        } else {
            result = false;
        }
        return result;*/


        try {
            if(file != null && file.exists()){

               // BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "utf-8"));
                /*PrintWriter pw = new PrintWriter(bw, true);
                pw.write(file_content);
                pw.flush();
                pw.close();*/
                bw.append(file_content);

                bw.flush();
                bw.close();


            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
            Log.i(TAG , e.getMessage());
            // 미친 .. 권한이 있는데 오류나는 이유는 뭔가 ?
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * (dir/file) 절대 경로 얻어오기
     *
     * @param file
     * @return String
     */
    private String getAbsolutePath(File file) {
        return "" + file.getAbsolutePath();
    }

    /**
     * (dir/file) 삭제 하기
     *
     * @param file
     */
    private boolean deleteFile(File file) {
        boolean result;
        if (file != null && file.exists()) {
            file.delete();
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    /**
     * 파일여부 체크 하기
     *
     * @param file
     * @return
     */
    private boolean isFile(File file) {
        boolean result;
        if (file != null && file.exists() && file.isFile()) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    /**
     * 디렉토리 여부 체크 하기
     *
     * @param dir
     * @return
     */
    private boolean isDirectory(File dir) {
        boolean result;
        if (dir != null && dir.isDirectory()) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    /**
     * 파일 존재 여부 확인 하기
     *
     * @param file
     * @return
     */
    private boolean isFileExist(File file) {
        boolean result;
        if (file != null && file.exists()) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    /**
     * 파일 이름 바꾸기
     *
     * @param file
     */
    private boolean reNameFile(File file, File new_name) {
        boolean result;
        if (file != null && file.exists() && file.renameTo(new_name)) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    /**
     * 디렉토리에 안에 내용을 보여 준다.
     *
     * @param dir
     * @return
     */
    private String[] getList(File dir) {
        if (dir != null && dir.exists())
            return dir.list();
        return null;
    }

    /**
     * 파일 읽어 오기
     *
     * @param file
     */
    private void readFile(File file) {
        int readcount = 0;
        if (file != null && file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                readcount = (int) file.length();
                byte[] buffer = new byte[readcount];
                fis.read(buffer);
               /* for(int i=0 ; i<file.length();i++){
                    Log.d(TAG, ""+buffer[i]);
                }*/
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 파일 복사
     *
     * @param file
     * @param save_file
     * @return
     */
    private boolean copyFile(File file, String save_file) {
        boolean result;
        if (file != null && file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                FileOutputStream newfos = new FileOutputStream(save_file);
                int readcount = 0;
                byte[] buffer = new byte[1024];
                while ((readcount = fis.read(buffer, 0, 1024)) != -1) {
                    newfos.write(buffer, 0, readcount);
                }
                newfos.close();
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            result = true;
        } else {
            result = false;
        }
        return result;
    }


}
