package whu.lbs;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.geotools.util.logging.Logging;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.IOException;

public class BaiduFlow {
    private static final Logger LOGGER = Logging.getLogger(BaiduFlow.class);
    private static final String
            headers = "'Host': 'huiyan.baidu.com', 'Connection': 'keep - alive', 'User - Agent': ' Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36', 'Accept': '* / *','Sec - Fetch - Site': 'same - site', 'Sec - Fetch - Mode': 'no - cors', 'Referer': 'https: // qianxi.baidu.com /', 'Accept - Encoding': 'gzip, deflate, br', 'Accept - Language': 'zh - CN, zh', 'q' : '0.9','cookie':' BIDUPSID=163D270C00008EE349117DD27AC58CBE; PSTM=1562302546; BAIDUID=5E9E0BBE6DFFC5779D1B5A326398786E:FG=1; delPer=0; PSINO=7; ZD_ENTRY=empty; H_WISE_SIDS=141176_114552_141192_139405_138496_135846_141000_139148_138471_138451_139193_138878_137978_140173_131247_132552_137746_138165_107317_138883_140260_141372_139057_140202_136863_138585_139171_140078_140114_136196_131861_140591_140324_140578_133847_140793_140065_131423_141175_140311_140839_136413_136752_110085_127969_140593_140865_139886_140993_139408_128200_138312_138426_141194_139557_140684_141191_140597_139600_140964; H_PS_PSSID=1450_21096_30495; BDORZ=B490B5EBF6F3CD402E515D22BCDA1598; PHPSESSID=9lg79hs0alm3ktedls42n17gb2";
    private static final String provincePrefix = "https://huiyan.baidu.com/migration/provincerank.jsonp?dt=country&id=";
    private static final String cityPrefix = "https://huiyan.baidu.com/migration/provincerank.jsonp?dt=country&id=";
    private int province = 1;
    private int citycode = 440300;
    private int typecode = 0;
    private String typeprefix = "&type=";
    private String typesuffix = "&date=";
    private String date = "20200101";
    private String suffix = "&callback=jsonp_1580799999370_2181648";

    String getUrl() {
        return (province == 1 ? provincePrefix : cityPrefix) + citycode + typeprefix + (typecode == 0 ? "move_in" : "move_out") + typesuffix + date + suffix;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getProvince() {
        return province;
    }

    public void setProvince(int province) {
        this.province = province;
    }

    public int getTypecode() {
        return typecode;
    }

    public void setTypecode(int typecode) {
        this.typecode = typecode;
    }

    public int getCitycode() {
        return citycode;
    }

    public void setCitycode(int citycode) {
        this.citycode = citycode;
    }

    public String action() {
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setContentCharset("utf-8");
        String ret = "";
        PostMethod post = new PostMethod(getUrl());
        LOGGER.log(Level.INFO, this::getUrl);
        post.setRequestHeader("header", headers);
        try {
            int rcode = httpClient.executeMethod(post);
            ret = post.getResponseBodyAsString();
            ret = ret.replaceFirst("jsonp_1580799999370_2181648\\(", "");
            ret = ret.substring(0, ret.length() - 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }
}
