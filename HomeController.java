package com.jd.ai.web.action;

import com.jd.ai.common.config.ConfLoader;
import com.jd.ai.common.utils.BeanJsonUtil;
import com.jd.ai.common.utils.CommonUtil;
import com.jd.ai.common.utils.Constants;
import com.jd.ai.domain.beans.OpBackResult;
import com.jd.ai.domain.beans.common.WorkTask;
import com.jd.ai.domain.beans.video.VideoInfo;
import com.jd.ai.service.video.VideoService;
import com.jd.ai.service.worktask.WorkTaskService;
import com.jd.common.web.LoginContext;
import com.jd.image.common.ImageUpload;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jd.ai.service.common.ToolUtil.getOpBackResult;


@Controller
@RequestMapping("/video-review")
public class HomeController extends SimpleFormController {
    private final static Logger log = LoggerFactory.getLogger(HomeController.class);

    @RequestMapping(value = "/home")
    public String homePage(HttpServletRequest request,Model view){
        LoginContext loginContext = LoginContext.getLoginContext();
        String erp = loginContext.getPin();				//erp账号
        if (StringUtils.isNotBlank(erp)){
            erp = erp.toLowerCase().trim();
        }
        view.addAttribute(Constants.LOGINED, erp);
         return "home";
    }

    @RequestMapping(value = "/login")
    public String login(HttpServletRequest request,HttpServletResponse response,Model view,@ModelAttribute(value = "username") String username,
                        @ModelAttribute(value = "password") String password){
        HashMap<String,String> loginAccountMap = ConfLoader.loginAccountMap;
        if (StringUtils.isNotBlank(username)&&StringUtils.isNotBlank(password)){
            if (loginAccountMap.containsKey(username)&&password.equals(loginAccountMap.get(username))){
                Cookie cookie = new Cookie(Constants.LOGINED, username);
                cookie.setDomain(ConfLoader.loginCookieDomain);
                cookie.setPath("/");
                cookie.setMaxAge(ConfLoader.loginCookieTime);
                response.addCookie(cookie);
                //request.getSession().setAttribute(Constants.LOGINED,username);
                //CommonUtil.setPermissions(request, view);
                String usernameStr = CommonUtil.getUserLoginCookie(request);
                if (StringUtils.isNotBlank(usernameStr)){
                    usernameStr = usernameStr.toLowerCase().trim();
                }
                view.addAttribute(Constants.LOGINED, usernameStr);
                return "home";
            }else{
                view.addAttribute("message", "用户名或密码错误");
                return "login";
            }
        }else{
            view.addAttribute("message", "用户名或密码为空");
            return "login";
        }
    }
    @RequestMapping(value = "/sqlExecute")
    public String sqlExecute() {
        return "common/sql";
    }

    @RequestMapping(value = "odCall", method = RequestMethod.POST)
    public void odCall(HttpServletRequest request, @RequestBody byte[] bytes, HttpServletResponse response) {
        OpBackResult opBackResult = new OpBackResult();
//        String imageSavePath = "";
        String dealStatus = "";
        try {
//            imageSavePath = request.getParameter("imgPath");
            dealStatus = request.getParameter("checkResult");
        } catch (Exception e) {
            log.error("odCall 获取参数异常", e);
        }
//        System.out.println("------------odCall---------------imageSavePath:" + imageSavePath);
        System.out.println("------------odCall---------------dealStatus:" + dealStatus);
        String returnJson = "";
        if (StringUtils.isBlank(dealStatus) || (StringUtils.isNotBlank(dealStatus) &&
                !NumberUtils.isNumber(dealStatus))) {
            opBackResult.setStatus(1001);
            opBackResult.setMessage("参数为空");
            opBackResult.setDataValue(StringUtils.isBlank(dealStatus) ? "参数checkResult为空" : "");
            returnJson = BeanJsonUtil.bean2Json(opBackResult);
            log.error(returnJson);
            CommonUtil.returnResponse(response, returnJson);
        } else {
            String videoDesc = "";
//            String[] imagePathFolder = imageSavePath.split("/");
//            if (imagePathFolder != null && imagePathFolder.length > 0) {
//                videoDesc = imagePathFolder[imagePathFolder.length - 1];
//            }
//            log.error("videoDesc:" + videoDesc);
//            try {
//                if (StringUtils.isNotBlank(videoDesc)) {
//                    Integer dealStatusInteger = Integer.parseInt(dealStatus);
//                    VideoInfo condition = new VideoInfo();
//                    condition.setVideoFolder(videoDesc);  //根据uuid获取视频信息
//                    VideoInfo identifyYellowVideo = videoService.findVideoInfoByCondition(condition);
//                    log.error("identifyYellowVideo:"+BeanJsonUtil.bean2Json(identifyYellowVideo));
////                    if (identifyYellowVideo != null && StringUtils.isNotBlank(identifyYellowVideo.getStreamUrl()) && StringUtils.isNotBlank(identifyYellowVideo.getStreamNo())) {
//                    if (identifyYellowVideo != null) {
//                        List<VideoResult> identifyYellowVideoResultList = uploadImage(imageSavePath, identifyYellowVideo, dealStatusInteger);
//                        VideoInfo update = new VideoInfo();
//                        update.setId(identifyYellowVideo.getId());
//                        update.setExecuteCount(1);
//                        update.setDealStatus(2);
//                        update.setCheckResult(dealStatusInteger);
//                        if (CollectionUtils.isNotEmpty(identifyYellowVideoResultList) && identifyYellowVideoResultList.size() > 0) {
//                            OpBackResult opBackResult1 = saveImages(identifyYellowVideoResultList, update);
//                            /*if (opBackResult1!=null&&opBackResult1.getStatus()==1006){
//                                boolean success= videoService.produceErpMessage(topic,batchId,messageId,String.valueOf(identifyYellowVideo.getVideoId()));
//                                log.error("--------------------------发送mq成功:---"+success);
//                            }else {
//                                log.error("saveImages 异常");
//                            }*/
//                            returnJson = BeanJsonUtil.bean2Json(opBackResult1);
//                        } else {
//                            List<VideoResult> identifyYellowVideoResultList1 = new ArrayList<VideoResult>();
//                            VideoResult result = new VideoResult();
//                            result.setVideoId(Long.valueOf(identifyYellowVideo.getVideoId().toString()));
//                            result.setStreamType(4);
//                            result.setStreamUrl(identifyYellowVideo.getStreamUrl());
//                            result.setStreamNo(Integer.parseInt(String.valueOf(identifyYellowVideo.getId())));
//                            result.setStreamCreateTime(new Date());
//                            result.setDealStatus(dealStatusInteger);
//                            result.setFrameUrl("");
//                            result.setCreateTime(new Date());
//                            identifyYellowVideoResultList1.add(result);
//                            OpBackResult opBackResult1 = saveImages(identifyYellowVideoResultList1, update);
//                            returnJson = BeanJsonUtil.bean2Json(opBackResult1);
//                        }

//                    } else {
//                        returnJson = BeanJsonUtil.bean2Json(getOpBackResult(1003, "该视频id不存在或者StreamUrl||StreamNo为空", null));
//                    }
//                } else {
//                    returnJson = BeanJsonUtil.bean2Json(getOpBackResult(1002, "获取视频文件夹名称异常", null));
//                }
//            } catch (Exception e) {
//                log.error("图片保存异常：", e);
//            }
            log.error("returnJson：" + BeanJsonUtil.bean2Json(returnJson));
            CommonUtil.returnResponse(response, returnJson);
        }
    }
//    public OpBackResult saveImages(List<VideoResult> identifyYellowVideoResultList1, VideoInfo identifyYellowVideo) {
//        OpBackResult returnJson = null;
//        try {
//            boolean success = videoService.addVideoResultUpdateStatus(identifyYellowVideoResultList1, identifyYellowVideo); //更新审核结果表，可以放在跑批里面
//            if (!success) {
//                returnJson = getOpBackResult(1005, "图片存储失败", null);
//            } else {
//                WorkTask workTask = new WorkTask();
//                workTask.setVideoId(identifyYellowVideo.getId());
//                workTask.setTaskType(4);  //鉴黄结果返回
//                workTask.setCreater("aii");
//                workTask.setCreateTime(new Date());
//
//                WorkTask workTask1 = workTaskService.addWorkTask(workTask);
//                if (workTask1 != null && workTask1.getId() > 0) {
//                    returnJson =getOpBackResult(1006, "图片保存成功，生成任务成功", null);
//                } else {
//                    returnJson = getOpBackResult(1007, "鉴黄结果返回任务失败", null);
//                }
//            }
//        } catch (Exception e) {
//            log.error("存储图片结果异常:", e);
//            returnJson = getOpBackResult(1004, "图片存储异常", null);
//        }
//        return returnJson;
//    }

    /**
     * 获取上传图片路径
     *
     * @param medicineCode
     * @return
     */
    public List<String> getImageUrls(String medicineCode) {
        List<String> imageUrl = new ArrayList<String>();
        File file = new File(medicineCode);
        List<String> dirFiles = getDirFileAddress(file, imageUrl);
        return dirFiles;
    }

    /**
     * 获取文件夹中的文件
     *
     * @param file
     * @param resultFileName
     * @return
     */
    private static List<String> getDirFileAddress(File file, List<String> resultFileName) {
        File[] files = file.listFiles();
        if (files == null) return resultFileName;// 判断目录下是不是空的
        for (File f : files) {
            if (f.isDirectory()) {// 判断是否文件夹
                //resultFileName.add(f.getPath()); // 文件夹路径不添加
                getDirFileAddress(f, resultFileName);// 调用自身,查找子目录
            } else
                resultFileName.add(f.getPath());
        }
        return resultFileName;
    }


    /**
     * 根据图片地址截取目录名称
     *
     * @param imageAddress
     * @return
     */
    public List<String> getImageAddressDirName(String imageAddress) {
        //D:\jkdj\O2O\hhs-goods-database\branches\v1.0_20150706\sds-web\target\sds-web\WEB-INF\attachment\images\laobaixing\100000\1.jpg
        Pattern p = Pattern.compile("(\\w+)");
        Matcher m = p.matcher(imageAddress);
        List<String> list = new ArrayList<String>();
        while (m.find()) {
            list.add(m.group(1));
        }
        return list;
    }


//    public List<VideoResult> uploadImage(String imageAddress, VideoInfo identifyYellowVideo, Integer dealStatusInteger) {
//        List<VideoResult> imageInfos = new ArrayList<VideoResult>();
//        List<String> dirFiles = getImageUrls(imageAddress);
//        if (CollectionUtils.isNotEmpty(dirFiles)) {
//            Collections.sort(dirFiles);
//            for (String url : dirFiles) {
//                File imagePath = new File(url);
//                long time = imagePath.lastModified();
//                Date imageDate = new Date(time);
//                List<String> imageDir = getImageAddressDirName(url);
//                if (CollectionUtils.isNotEmpty(imageDir) && imageDir.size() > 3) {
//                    int imageDirLength = imageDir.size();
//                    /*String imagewenjianjia = imageDir.get(imageDirLength - 3);
//                    String imageName = imageDir.get(imageDirLength - 2);
//                    String iamgeGeshi = imageDir.get(imageDirLength - 1);*/
//                    if (imagePath != null) {
//                        try {
//                            //BufferedImage bufferedImage = ImageIO.read(imagePath);
//                            //int width = bufferedImage.getWidth();
//                            //int height = bufferedImage.getHeight();
//                            String address = ImageUpload.uploadFile(imagePath, ConfLoader.imageUploadAucode);
//                            System.out.println("--------upload---------" + address);
//                            JSONArray jsonArray = JSONArray.fromObject(address);
//                            if (jsonArray != null & jsonArray.size() > 0) {
//                                JSONObject obj = jsonArray.getJSONObject(0);
//                                String id = obj.getString("id");
//                                if (StringUtils.isNotBlank(id) && "1".equals(id)) {
//                                    String msg = obj.getString("msg");
//                                    //StringBuffer sb = new StringBuffer();
//                                    //sb.append("s").append(width).append("x").append(height).append("_");
//                                    VideoResult result = new VideoResult();
//                                    result.setVideoId(Long.valueOf(identifyYellowVideo.getVideoId().toString()));
//                                    result.setStreamType(identifyYellowVideo.getStreamType());
//                                    result.setStreamUrl(identifyYellowVideo.getStreamUrl());
//                                    result.setStreamNo(Integer.parseInt(String.valueOf(identifyYellowVideo.getId())));
//                                    result.setStreamCreateTime(imageDate);
//                                    result.setDealStatus(dealStatusInteger);
//                                    //result.setFrameUrl(ConfLoader.imagePath + sb.toString() + msg);
//                                    result.setFrameUrl(msg);
//                                    result.setCreateTime(new Date());
//                                    imageInfos.add(result);
//                                }
//                            }
//                        } catch (Exception e) {
//                            log.error("uploadImage 上传图片异常：", e);
//                        }
//                    }
//                }
//            }
//        }
//        return imageInfos;
//    }

}
