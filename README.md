# ImgTranslator
基于tesseract 的OCR实时扫描工具，目前只有手机号扫描

  
  使用方法：
  
      public void scanPhone{
        Bitmap bmp=需要识别的图片，在扫描识别的场景中，就是相机预览图中取出的扫描区域;
        Translator translator = new PhoneNumberTranslator();
        //开始识别
        ImageTranslator.getInstance().translate(translator, rotateToDegrees(bmp, 90), new ImageTranslator.TesseractCallback() {
          @Override
          public void onResult(String result) {
            Log.d("scantest", "扫描结果：  " + result);
            }
          @Override
          public void onFail(String reason) {
            Log.d("scantest", "解析失败：  " + reason);
            }
          });
      }
  
  这种方法还可以针对 身份证扫描、邮箱扫描、银行卡号 等做相应的识别算法，如果有感兴趣的朋友愿意分享自己的算法，非常欢迎提交代码，提交代码格式如下：
  
  以手机号识别为例，我创建了一个PhoneNumberTranslator类
  假如你想实现一个邮箱扫描：
  
  1、实现一个算法类,继承Translator
  
    public class EmailTranslator extends Translator{
    /**
    * 你使用的字库名字
    */
    @Override
    public String initLanguage() {
        return "email";
    }

    /**
    * @params 从相机预览图中传入的 扫描区域Bitmap
    * 在这里实现你对图片中的email的过滤、捕捉等处理，然后返回捕捉到的email区域bitmap
    * 如果可以断定图片中没有email，return null即可
    */
    @Override
    public Bitmap catchText(Bitmap bitmap) {
        return emailBitmap;
    }

     /**
    * 对于扫描结果的筛选
    * 如果catchText() 捕捉到了email，那么这个包含email的Bitmap会交由 tess-two识别，最终的识别结果，会用正则公式来筛选需要的内容
    * 比如这里返回了一个email的正则表达式，最终会将识别结果中的所有email返回，如果不需要筛选，这里return "" 即可
    */
    @Override
    public String filterRule() {
        return "^(\w)+(\.\w+)*@(\w)+((\.\w+)+)$";
    }
    }

2、提交你的字库
   将你使用的字库文件提交到 https://github.com/SiKang123/tessdata ，比如这里用的是email字库，那么就将email.traineddata 文件，提交到这个地址
   
3、提交你的代码，我测试后，上线代码
