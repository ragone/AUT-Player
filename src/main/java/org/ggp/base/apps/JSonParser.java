//package org.ggp.base.apps;
//
//import com.oracle.tools.packager.IOUtils;
//import external.JSON.JSONArray;
//import external.JSON.JSONException;
//import external.JSON.JSONObject;
//
//import java.io.*;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Iterator;
//import java.util.List;
//
///**
// * TODO: Description of JSonParser.
// *
// * @author ragone.
// * @version 3/06/16
// */
//public class JSonParser {
//
//    public static void main(String[] args) throws FileNotFoundException, JSONException {
//        new JSonParser();
//    }
//
//    public JSonParser() throws FileNotFoundException, JSONException {
//        List<File> files = getFiles();
//        for (File file : files) {
//                InputStream is = new FileInputStream(file);
//                String jsonTxt = IOUtils.toString(is);
//                System.out.println(jsonTxt);
//                JSONObject json = new JSONObject(jsonTxt);
//                String a = json.getString("1000");
//                System.out.println(a);
//            }
//        }
//    }
//
//    private List<File> getFiles() {
//
//        File dir = new File("/Users/ragone/IdeaProjects/ggp-base/tic_5/");
//        List<File> list = Arrays.asList(dir.listFiles(new FilenameFilter(){
//            @Override
//            public boolean accept(File dir, String name) {
//                return name.endsWith(".json"); // or something else
//            }}));
//
//        return list;
//
//    }
//}
//
