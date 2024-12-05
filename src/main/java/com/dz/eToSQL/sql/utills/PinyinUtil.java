package com.dz.eToSQL.sql.utills;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 拼音转换工具类
 */
public class PinyinUtil {
    private static final HanyuPinyinOutputFormat FORMAT = new HanyuPinyinOutputFormat();
    
    static {
        FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }
    
    /**
     * 将中文转换为拼音
     * @param chinese 中文字符串
     * @param separator 分隔符
     * @return 转换后的拼音
     */
    public static String getPinyin(String chinese, String separator) {
        StringBuilder pinyinBuilder = new StringBuilder();
        char[] chars = chinese.toCharArray();
        
        for (char c : chars) {
            if (Character.toString(c).matches("[\\u4E00-\\u9FA5]+")) {
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, FORMAT);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        pinyinBuilder.append(pinyinArray[0]).append(separator);
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    // 如果转换失败,保留原字符
                    pinyinBuilder.append(c).append(separator);
                }
            } else {
                pinyinBuilder.append(c).append(separator);
            }
        }
        
        String result = pinyinBuilder.toString();
        return separator.isEmpty() ? result : result.substring(0, result.length() - separator.length());
    }
} 