package com.pt.vx.utils;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.ChineseDate;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.pt.vx.config.AllConfig;
import com.pt.vx.domain.Api.*;
import com.pt.vx.domain.BirthDay;
import com.pt.vx.domain.story.Catalogue;
import com.pt.vx.domain.story.Chapter;
import com.pt.vx.domain.story.ChapterData;
import org.apache.commons.lang3.StringEscapeUtils;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class ApiUtil {

    private static final Logger logger = Logger.getAnonymousLogger();

    private static final String  history_today= "http://api.weijieyue.cn/api/lsjt/api.php?max=%d";//历史上的今天

    private static final String  qinghua = "https://api.uomg.com/api/rand.qinghua?format=json"; //土味情话

    private static final String en = "https://api.vvhan.com/api/en";//励志英语

    private static final String WorldRead60sApi="https://api.vvhan.com/api/60s?type=json";//世界新闻

    private static final String joke ="https://api.vvhan.com/api/joke?type=json";//每日笑话
    // http://api.weijieyue.cn/api/tgrj/api.php
    private static final String tgrj ="http://api.gt5.cc/api/dog";//舔狗日记

    private static final String randomRead ="https://api.vvhan.com/api/ian?type=json";//随机一句

    private static final String horoscopeApi = "https://api.vvhan.com/api/horoscope?type=%s&time=today";//星座运势 aries, taurus, gemini, cancer, leo, virgo, libra, scorpio, sagittarius, capricorn, aquarius, pisces

    private static final String storyApi = "https://api.pingcc.cn/fiction/search/title/%s/1/10";

    private static final String storyApiChapter ="https://api.pingcc.cn/fictionChapter/search/%s";

    private static final String storyApiContent="https://api.pingcc.cn/fictionContent/search/%s";


    public static String getStoryApiContent()  {
        String re ;
        try {
            re = HttpUtil.get(String.format(storyApiContent, getStoryApiChapter()));
        } catch (Exception e) {
            logger.warning(String.format("获取小说失败 %s", e.getMessage()));
            return null;
        }
        re =  StringEscapeUtils.unescapeJava(re);
        return JSONUtil.toBean(re, Result.class).getData();
    }
    private static String getStoryApiChapter() throws InterruptedException {
        Thread.sleep(501);
        LocalDate satrtTime = AllConfig.start_time;
        String result = HttpUtil.get(String.format(storyApiChapter, getStoryCatalogue()));
        result =  StringEscapeUtils.unescapeJava(result);
        String data = JSONUtil.toBean(result, Result.class).getData();
        ChapterData chapterData = JSONUtil.toBean(data, ChapterData.class);
        String index = DateUtil.passDayOfNow(satrtTime);
        Chapter chapter = chapterData.getChapterList().get(Integer.parseInt(index));
        return chapter.getChapterId();
    }
    private static String getStoryCatalogue() throws InterruptedException {
        Thread.sleep(502);
        String title =  AllConfig.title;
        String resultGet = HttpUtil.get(String.format(storyApi, title));
        if(ObjectUtil.isEmpty(resultGet)){
            logger.warning("获取小说失败");
            return null;
        }
        resultGet =  StringEscapeUtils.unescapeJava(resultGet);
        Result result = JSONUtil.toBean(resultGet, Result.class);
        String data = result.getData();
        List<Catalogue> catalogues = JSONUtil.toList(data, Catalogue.class);
        if(CollectionUtil.isNotEmpty(catalogues)){
            for(Catalogue catalogue : catalogues){
                if( AllConfig.author.equals(catalogue.getAuthor())){
                    return catalogue.getFictionId();
                }
            }
        }
        Catalogue catalogue = Optional.ofNullable(catalogues).orElse(new ArrayList<>()).stream().findFirst().orElse(null);
        return catalogue == null ? null : catalogue.getFictionId();
    }

    /**
     * 获取历史上今天
     * @param count 数量，最大20
     * @return count条历史上今天
     */
    public static String getHistoryToday(int count){
        return HttpUtil.get(String.format(history_today, count));
    }

    /**
     * @return 获取一条土味情话
     */
    public static String getQingHua(){
        String result = HttpUtil.get(qinghua);
        result =  StringEscapeUtils.unescapeJava(result);
        QingHuaDto qingHuaDto = JSONUtil.toBean(result, QingHuaDto.class);
        return qingHuaDto.getContent();
    }

    /**
     * @return 每日英语
     */
    public static String getEnglish(){
        String re = HttpUtil.get(en);
        re =  StringEscapeUtils.unescapeJava(re);
        Result result = JSONUtil.toBean(re, Result.class);
        String data = result.getData();
        EnglishDto en = JSONUtil.toBean(data, EnglishDto.class);
        String format = String.format("获取每日英语：%s", result);
        logger.info(format);
        return en.getEn();
    }

    /**
     *
     * @return 世界新闻
     */
    public static String getWorldRead60s(){
        String result = HttpUtil.get(WorldRead60sApi);
        result =  StringEscapeUtils.unescapeJava(result);
        WorldRead60s wordRead60s = JSONUtil.toBean(result, WorldRead60s.class);
        StringBuilder builder = new StringBuilder();
        wordRead60s.getData().forEach(x-> builder.append(x).append("\n"));
        return builder.toString();
    }

    /**
     *
     * @return 笑话
     */
    public static String getJoke(){
        String result = HttpUtil.get(joke);
        result =  StringEscapeUtils.unescapeJava(result);
        JokeDto jokeDto = JSONUtil.toBean(result, JokeDto.class);
        return "《" + jokeDto.getTitle() + "》" + "\n" + jokeDto.getJoke();
    }

    /**
     *
     * @return 舔狗日记
     */
    public static String getTgrj(){
        return HttpUtil.get(tgrj);
    }

    /**
     *
     * @return 随机一句
     */
    public static String getRandomRead(){
        String result = HttpUtil.get(randomRead);
        result =  StringEscapeUtils.unescapeJava(result);
        RandomRead read = JSONUtil.toBean(result, RandomRead.class);
        RandomData data = read.getData();
        return data.getVhan()+" -- "+data.getSource();
    }

    /**
     *
     * @param horoscope 星座
     * @return 每日星座情况
     */
    public static String getHoroscopeRead(String horoscope){
        String result = HttpUtil.get(String.format(horoscopeApi, horoscope));
        if(ObjectUtil.isEmpty(result)){
            logger.warning(String.format("获取星座解析失败，星座为：%s",horoscope ));
            return "";
        }
        result =  StringEscapeUtils.unescapeJava(result);
        Result result1 = JSONUtil.toBean(result, Result.class);
        HoroscopeDto horoscopeDto = JSONUtil.toBean(result1.getData(), HoroscopeDto.class);
        Fortunetext fortunetext = horoscopeDto.getFortunetext();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("《")
                .append(horoscopeDto.getTitle())
                .append("》").append("\n")
                .append("幸运色:")
                .append(horoscopeDto.getLuckycolor())
                .append("\n")
                .append("幸运数字:")
                .append(horoscopeDto.getLuckynumber())
                .append("\n")
                .append("概况:")
                .append(horoscopeDto.getShortcomment())
                .append("\n")
                .append("整体情况:")
                .append(fortunetext.getAll())
                .append("\n")
                .append("事业情况:")
                .append(fortunetext.getWork())
                .append("\n")
                .append("爱情情况:")
                .append(fortunetext.getLove())
                .append("\n")
                .append("财运情况:")
                .append(fortunetext.getMoney())
                .append("\n")
                .append("健康情况:")
                .append(fortunetext.getHealth());
        return stringBuilder.toString();
    }

    public static String getHoroscopeRead(BirthDay birthDay){
        return getHoroscopeRead(getHoroscope(birthDay));
    }

    private static String getHoroscope(String horoscope){
        if("白羊".equals(horoscope) || "白羊座".equals(horoscope)){
            return "aries";
        }else if("金牛".equals(horoscope) || "金牛座".equals(horoscope)){
            return "taurus";
        }else if("双子".equals(horoscope) || "双子座".equals(horoscope)){
            return "gemini";
        }else if("巨蟹".equals(horoscope) || "巨蟹座".equals(horoscope)){
            return "cancer";
        }else if("狮子".equals(horoscope) || "狮子座".equals(horoscope)){
            return "leo";
        }else if("处女".equals(horoscope) || "处女座".equals(horoscope)){
            return "virgo";
        }else if("天蝎".equals(horoscope) || "天蝎座".equals(horoscope)){
            return "libra";
        }else if("射手".equals(horoscope) || "射手座".equals(horoscope)){
            return "scorpio";
        }else if("摩羯".equals(horoscope) || "摩羯座".equals(horoscope)){
            return "sagittarius";
        }else if("水瓶".equals(horoscope) || "水瓶座".equals(horoscope)){
            return "capricorn";
        }else if("双鱼".equals(horoscope) || "双鱼座".equals(horoscope)){
            return "aquarius";
        }else if("天秤".equals(horoscope) || "天秤座".equals(horoscope)){
            return "pisces";
        }else {
            logger.warning("请天写正确的星座！"+"horoscope");
            throw new RuntimeException("请天写正确的星座");
        }
    }

    private static String getHoroscope(BirthDay birthDay){

        int month = birthDay.getMonth();
        int day = birthDay.getDay();
        boolean chinese = birthDay.isChinese();
        if(chinese){
            int year = birthDay.getYear();
            ChineseDate chineseDate=new ChineseDate(year,month,day);
            month = chineseDate.getGregorianMonthBase1();
            day = chineseDate.getGregorianDay();
        }
        if(month == 3 && day >= 21  ||  month == 4 && day <=20){
            return getHoroscope("白羊");
        }else if(month == 4 || month == 5 && day <= 20){
            return getHoroscope("金牛");
        }else if(month == 5 || month == 6 && day <= 20){
            return getHoroscope("双子");
        }else if(month == 6 || month == 7 && day <= 22){
            return getHoroscope("巨蟹");
        }else if(month == 7 || month == 8 && day <= 22){
            return getHoroscope("狮子");
        }else if(month == 8 || month == 9 && day <= 22){
            return getHoroscope("处女");
        }else if(month == 9 || month == 10 && day <= 22){
            return getHoroscope("天秤");
        }else if(month == 10 || month == 11 && day <= 22){
            return getHoroscope("天蝎");
        }else if(month == 11 || month == 12 && day <= 22){
            return getHoroscope("射手");
        }else if(month == 12 || month == 1 && day <= 21){
            return getHoroscope("摩羯");
        }else if(month == 1 || month == 2 && day <= 19){
            return getHoroscope("水瓶");
        }else if(month == 2 || month == 3){
            return getHoroscope("双鱼");
        }
        return  getHoroscope("不知道");
    }


}
