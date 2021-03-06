package com.oneworld.back.controller;

import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSONObject;
import com.oneworld.back.controller.easyexcel.ArticleModel;
import com.oneworld.back.controller.easyexcel.ExcelListener;
import com.oneworld.back.service.ArticleService;
import com.oneworld.back.utils.CommonUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * @description: 文章相关Controller
 */
@RestController
@RequestMapping("/article")
public class ArticleController {

	@Autowired
	private ArticleService articleService;

	/**
	 * 查询文章列表
	 */
	@RequiresPermissions("article:list")
	@GetMapping("/listArticle")
	public JSONObject listArticle(HttpServletRequest request) {
		return articleService.listArticle(CommonUtil.request2Json(request));
	}

	/**
	 * 新增文章
	 */
	@RequiresPermissions("article:add")
	@PostMapping("/addArticle")
	public JSONObject addArticle(@RequestBody JSONObject requestJson) {
		CommonUtil.hasAllRequired(requestJson, "title,content,bgmImg");
		return articleService.addArticle(requestJson);
	}

	/**
	 * 修改文章
	 */
	@RequiresPermissions("article:update")
	@PostMapping("/updateArticle")
	public JSONObject updateArticle(@RequestBody JSONObject requestJson) {
		CommonUtil.hasAllRequired(requestJson, "id,title,content,bgmImg");
		return articleService.updateArticle(requestJson);
	}

	/**
	 * 删除文章
	 * */
	@PostMapping("/deleteArticle")
	public JSONObject deleteArticle(@RequestBody JSONObject requestJson) {
		CommonUtil.hasAllRequired(requestJson, "id");
		return articleService.deleteArticle(requestJson);
	}

	/**
	 * 批量删除文章
	 * */
	@PostMapping("/deleteArticles")
	public JSONObject deleteArticles(@RequestBody Long[] deleteArr){
		return articleService.deleteArticles(deleteArr);
	}

	/**
	 * 导入
	 * */
	@RequestMapping("/importExcel")
	public Map importExcel(@RequestParam("file") MultipartFile file)throws IOException {
		Map returnMap = new HashMap();
		InputStream inputStream = file.getInputStream();
		ExcelListener listener = new ExcelListener();
		try {
			ExcelReader excelReader = new ExcelReader(inputStream,null,listener);
			excelReader.read(new Sheet(1,1, ArticleModel.class));
		}catch (ExcelAnalysisException e){
			e.printStackTrace();
			returnMap.put("code",1);
			returnMap.put("msg","请使用谷歌浏览器！");
			return returnMap;
		}
		List<Object> list = listener.getDatas();
		//转换数据类型,并插入到数据库
		for (int i = 0; i < list.size(); i++) {
			ArticleModel articleModel = (ArticleModel) list.get(i);
			//插入数据库
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("content",articleModel.getContent());
			articleService.addArticle(jsonObject);
		}
		returnMap.put("code",0);
		returnMap.put("msg","导入成功！");
		return returnMap;
	}

	/**
	 * 导出
	 * */
	@RequestMapping(value = "/exportExcel")
	public void exportExcel(HttpServletResponse response) throws IOException {
		ExcelWriter writer = null;
		OutputStream outputStream = response.getOutputStream();
		try {
			//添加响应头信息
			response.setHeader("Content-disposition", "attachment; filename=" + "catagory.xls");
			response.setContentType("application/msexcel;charset=UTF-8");
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);
			response.setHeader("fileName","errorCode.xlsx");

			//实例化 ExcelWriter
			writer = new ExcelWriter(outputStream, ExcelTypeEnum.XLSX, true);

			//实例化表单
			Sheet sheet = new Sheet(1, 0, ArticleModel.class);
			sheet.setSheetName("errorCode列表");

			//获取数据
			List<JSONObject> list = articleService.selectAll();
			List<ArticleModel> articleModelList = new ArrayList<>();
			Iterator iterator = list.iterator();
			while (iterator.hasNext()){
				JSONObject jsonObject = (JSONObject) iterator.next();
				ArticleModel articleModel = new ArticleModel();
				articleModel.setContent(jsonObject.get("content").toString());
				articleModelList.add(articleModel);
			}
			//输出
			writer.write(articleModelList, sheet);
			writer.finish();
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				response.getOutputStream().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@RequestMapping(value = "/uploadImg")
	@CrossOrigin
	public String uploadImg(HttpServletRequest request,@RequestParam("image") MultipartFile image)throws IOException{
		/*String result = "error";
		if(!image.isEmpty()){
			IPTimeStamp ipTimeStamp = new IPTimeStamp(image.getOriginalFilename());
			String fileName ="";
			fileName=ipTimeStamp.getIPTimeRand();
			image.transferTo(new File(request.getSession().getServletContext().getRealPath("/")+fileName));
			result = request.getSession().getServletContext().getRealPath("/")+fileName;
		}
		System.out.println(result);
		return result;*/
		InputStream inputStream = null;
		OutputStream os = null;
		String path = null;
		String fileName = System.currentTimeMillis()+"_"+image.getOriginalFilename();
		try{
			byte[] bs = new byte[1024];
			// 读取到的数据长度
			int len;
			// 输出的文件流保存到本地文件
			File tempFile = new File("D:\\upload");
			if (!tempFile.exists()) {
				tempFile.mkdirs();
			}
			inputStream = image.getInputStream();
			path = tempFile.getPath() + File.separator + fileName;
			os = new FileOutputStream(path);
			// 开始读取
			while ((len = inputStream.read(bs)) != -1) {
				os.write(bs, 0, len);
			}
		}catch (IOException e){
			e.printStackTrace();
		}finally {
			// 完毕，关闭所有链接
			try {
				os.close();
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return fileName;
	}
}
