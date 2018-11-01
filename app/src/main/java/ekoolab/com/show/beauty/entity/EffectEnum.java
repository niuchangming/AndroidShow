package ekoolab.com.show.beauty.entity;

import com.faceunity.entity.Effect;

import java.util.ArrayList;

import ekoolab.com.show.R;

/**
 * Created by tujh on 2018/1/30.
 */

public enum EffectEnum {

    EffectNone("none", R.mipmap.delete_all, "none", 1, Effect.EFFECT_TYPE_NONE, ""),

    Effect_fengya_ztt_fu("fengya_ztt_fu", R.mipmap.effect_fengya_ztt_fu, "normal/fengya_ztt_fu.bundle", 4, Effect.EFFECT_TYPE_NORMAL, ""),
    Effect_hudie_lm_fu("hudie_lm_fu", R.mipmap.effect_hudie_lm_fu, "normal/hudie_lm_fu.bundle", 4, Effect.EFFECT_TYPE_NORMAL, ""),
    Effect_touhua_ztt_fu("touhua_ztt_fu", R.mipmap.effect_touhua_ztt_fu, "normal/touhua_ztt_fu.bundle", 4, Effect.EFFECT_TYPE_NORMAL, ""),
    Effect_juanhuzi_lm_fu("juanhuzi_lm_fu", R.mipmap.effect_juanhuzi_lm_fu, "normal/juanhuzi_lm_fu.bundle", 4, Effect.EFFECT_TYPE_NORMAL, ""),
    Effect_mask_hat("mask_hat", R.mipmap.mask_hat, "normal/mask_hat.bundle", 4, Effect.EFFECT_TYPE_NORMAL, ""),
    Effect_yazui("yazui", R.mipmap.effect_yazui, "normal/yazui.bundle", 4, Effect.EFFECT_TYPE_NORMAL, ""),
    Effect_yuguan("yuguan", R.mipmap.effect_yuguan, "normal/yuguan.bundle", 4, Effect.EFFECT_TYPE_NORMAL, ""),

    Effect_afd("afd", R.mipmap.effect_afd, "ar/afd.bundle", 4, Effect.EFFECT_TYPE_AR, ""),
    Effect_baozi("baozi", R.mipmap.effect_baozi, "ar/baozi.bundle", 4, Effect.EFFECT_TYPE_AR, ""),
    Effect_tiger("tiger", R.mipmap.effect_tiger, "ar/tiger.bundle", 4, Effect.EFFECT_TYPE_AR, ""),
    Effect_xiongmao("xiongmao", R.mipmap.effect_xiongmao, "ar/xiongmao.bundle", 4, Effect.EFFECT_TYPE_AR, ""),
    Effect_armesh("armesh", R.mipmap.effect_armesh, "ar/armesh.bundle", 4, Effect.EFFECT_TYPE_AR, ""),
    Effect_armesh_ex("armesh_ex", R.mipmap.effect_armesh_ex, "ar/armesh_ex.bundle", 4, Effect.EFFECT_TYPE_AR, "AR面具高精度版"),

    Effect_mask_liudehua("mask_liudehua", R.mipmap.mask_liudehua, "change/mask_liudehua.bundle", 4, Effect.EFFECT_TYPE_FACE_CHANGE, ""),
    Effect_mask_linzhiling("mask_linzhiling", R.mipmap.mask_linzhiling, "change/mask_linzhiling.bundle", 4, Effect.EFFECT_TYPE_FACE_CHANGE, ""),
    Effect_mask_luhan("mask_luhan", R.mipmap.mask_luhan, "change/mask_luhan.bundle", 4, Effect.EFFECT_TYPE_FACE_CHANGE, ""),
    Effect_mask_guocaijie("mask_guocaijie", R.mipmap.mask_guocaijie, "change/mask_guocaijie.bundle", 4, Effect.EFFECT_TYPE_FACE_CHANGE, ""),
    Effect_mask_huangxiaoming("mask_huangxiaoming", R.mipmap.mask_huangxiaoming, "change/mask_huangxiaoming.bundle", 4, Effect.EFFECT_TYPE_FACE_CHANGE, ""),
    Effect_mask_matianyu("mask_matianyu", R.mipmap.mask_matianyu, "change/mask_matianyu.bundle", 4, Effect.EFFECT_TYPE_FACE_CHANGE, ""),
    Effect_mask_tongliya("mask_tongliya", R.mipmap.mask_tongliya, "change/mask_tongliya.bundle", 4, Effect.EFFECT_TYPE_FACE_CHANGE, ""),

    Effect_future_warrior("future_warrior", R.mipmap.effect_future_warrior, "expression/future_warrior.bundle", 4, Effect.EFFECT_TYPE_EXPRESSION, "张嘴试试"),
    Effect_jet_mask("jet_mask", R.mipmap.effect_jet_mask, "expression/jet_mask.bundle", 4, Effect.EFFECT_TYPE_EXPRESSION, "鼓腮帮子"),
    Effect_sdx2("sdx2", R.mipmap.effect_sdx2, "expression/sdx2.bundle", 1, Effect.EFFECT_TYPE_EXPRESSION, "皱眉试试"),
    Effect_luhantongkuan_ztt_fu("luhantongkuan_ztt_fu", R.mipmap.effect_luhantongkuan_ztt_fu, "expression/luhantongkuan_ztt_fu.bundle", 4, Effect.EFFECT_TYPE_EXPRESSION, "眨一眨眼"),
    Effect_qingqing_ztt_fu("qingqing_ztt_fu", R.mipmap.effect_qingqing_ztt_fu, "expression/qingqing_ztt_fu.bundle", 4, Effect.EFFECT_TYPE_EXPRESSION, "嘟嘴试试"),
    Effect_xiaobianzi_zh_fu("xiaobianzi_zh_fu.bundle", R.mipmap.effect_xiaobianzi_zh_fu, "expression/xiaobianzi_zh_fu.bundle", 4, Effect.EFFECT_TYPE_EXPRESSION, "微笑触发"),
    Effect_xiaoxueshen_ztt_fu("xiaoxueshen_ztt_fu", R.mipmap.effect_xiaoxueshen_ztt_fu, "expression/xiaoxueshen_ztt_fu.bundle", 4, Effect.EFFECT_TYPE_EXPRESSION, "吹气触发"),

    Effect_chiji_lm_fu("chiji_lm_fu", R.mipmap.effect_chiji_lm_fu, "background/chiji_lm_fu.bundle", 1, Effect.EFFECT_TYPE_BACKGROUND, ""),
    Effect_ice_lm_fu("ice_lm_fu", R.mipmap.effect_ice_lm_fu, "background/ice_lm_fu.bundle", 1, Effect.EFFECT_TYPE_BACKGROUND, ""),
    Effect_jingongmen_zh_fu("jingongmen_zh_fu", R.mipmap.effect_jingongmen_zh_fu, "background/jingongmen_zh_fu.bundle", 1, Effect.EFFECT_TYPE_BACKGROUND, ""),
    Effect_gufeng_zh_fu("gufeng_zh_fu", R.mipmap.effect_gufeng_zh_fu, "background/gufeng_zh_fu.bundle", 1, Effect.EFFECT_TYPE_BACKGROUND, ""),
    Effect_men_ztt_fu("men_ztt_fu", R.mipmap.effect_men_ztt_fu, "background/men_ztt_fu.bundle", 1, Effect.EFFECT_TYPE_BACKGROUND, ""),
    Effect_hez_ztt_fu("hez_ztt_fu", R.mipmap.effect_hez_ztt_fu, "background/hez_ztt_fu.bundle", 1, Effect.EFFECT_TYPE_BACKGROUND, "转头触发"),

    Effect_fu_lm_koreaheart("fu_lm_koreaheart", R.mipmap.effect_fu_lm_koreaheart, "gesture/fu_lm_koreaheart.bundle", 4, Effect.EFFECT_TYPE_GESTURE, "单手手指比心"),
    Effect_fu_zh_baoquan("fu_zh_baoquan", R.mipmap.effect_fu_zh_baoquan, "gesture/fu_zh_baoquan.bundle", 4, Effect.EFFECT_TYPE_GESTURE, "双手抱拳"),
    Effect_fu_zh_hezxiong("fu_zh_hezxiong", R.mipmap.effect_fu_zh_hezxiong, "gesture/fu_zh_hezxiong.bundle", 4, Effect.EFFECT_TYPE_GESTURE, "双手合十"),
    Effect_fu_ztt_live520("fu_ztt_live520", R.mipmap.effect_fu_ztt_live520, "gesture/fu_ztt_live520.bundle", 4, Effect.EFFECT_TYPE_GESTURE, "双手比心"),
    Effect_ssd_thread_thumb("ssd_thread_thumb", R.mipmap.effect_ssd_thread_thumb, "gesture/ssd_thread_thumb.bundle", 4, Effect.EFFECT_TYPE_GESTURE, "竖个拇指"),
    Effect_ssd_thread_six("ssd_thread_six", R.mipmap.effect_ssd_thread_six, "gesture/ssd_thread_six.bundle", 4, Effect.EFFECT_TYPE_GESTURE, "比个六"),
    Effect_ssd_thread_cute("ssd_thread_cute", R.mipmap.effect_ssd_thread_cute, "gesture/ssd_thread_cute.bundle", 4, Effect.EFFECT_TYPE_GESTURE, "双拳靠近脸颊卖萌"),

    Effect_baimao_Animoji("baimao_Animoji", R.mipmap.effect_baimao_animoji, "animoji/baimao_Animoji.bundle", 1, Effect.EFFECT_TYPE_ANIMOJI, ""),
    Effect_chaiquan_Animoji("chaiquan_Animoji", R.mipmap.effect_chaiquan_animoji, "animoji/chaiquan_Animoji.bundle", 1, Effect.EFFECT_TYPE_ANIMOJI, ""),
    Effect_douniuquan_Animoji("douniuquan_Animoji", R.mipmap.effect_douniuquan_animoji, "animoji/douniuquan_Animoji.bundle", 1, Effect.EFFECT_TYPE_ANIMOJI, ""),
    Effect_kulutou_Animoji("kulutou_Animoji", R.mipmap.effect_kulutou_animoji, "animoji/kulutou_Animoji.bundle", 1, Effect.EFFECT_TYPE_ANIMOJI, ""),
    Effect_maotouying_Animoji("maotouying_Animoji", R.mipmap.effect_maotouying_animoji, "animoji/maotouying_Animoji.bundle", 1, Effect.EFFECT_TYPE_ANIMOJI, ""),

    Effect_picasso_e1("picasso_e1", R.mipmap.effect_picasso_e1, "portrait_drive/picasso_e1.bundle", 1, Effect.EFFECT_TYPE_PORTRAIT_DRIVE, ""),
    Effect_picasso_e2("picasso_e2", R.mipmap.effect_picasso_e2, "portrait_drive/picasso_e2.bundle", 1, Effect.EFFECT_TYPE_PORTRAIT_DRIVE, ""),
    Effect_picasso_e3("picasso_e3", R.mipmap.effect_picasso_e3, "portrait_drive/picasso_e3.bundle", 1, Effect.EFFECT_TYPE_PORTRAIT_DRIVE, ""),

    Effect_facewarp2("facewarp2", R.mipmap.effect_facewarp2, "facewarp/facewarp2.bundle", 4, Effect.EFFECT_TYPE_FACE_WARP, ""),
    Effect_facewarp3("facewarp3", R.mipmap.effect_facewarp3, "facewarp/facewarp3.bundle", 4, Effect.EFFECT_TYPE_FACE_WARP, ""),
    Effect_facewarp4("facewarp4", R.mipmap.effect_facewarp4, "facewarp/facewarp4.bundle", 4, Effect.EFFECT_TYPE_FACE_WARP, ""),
    Effect_facewarp5("facewarp5", R.mipmap.effect_facewarp5, "facewarp/facewarp5.bundle", 4, Effect.EFFECT_TYPE_FACE_WARP, ""),
    Effect_facewarp6("facewarp6", R.mipmap.effect_facewarp6, "facewarp/facewarp6.bundle", 4, Effect.EFFECT_TYPE_FACE_WARP, ""),

    Effect_douyin_old("douyin_01", R.mipmap.effect_douyin_old, "musicfilter/douyin_01.bundle", 4, Effect.EFFECT_TYPE_MUSIC_FILTER, ""),
    Effect_douyin("douyin_02", R.mipmap.effect_douyin, "musicfilter/douyin_02.bundle", 4, Effect.EFFECT_TYPE_MUSIC_FILTER, "");

    private String bundleName;
    private int resId;
    private String path;
    private int maxFace;
    private int effectType;
    private String description;

    EffectEnum(String name, int resId, String path, int maxFace, int effectType, String description) {
        this.bundleName = name;
        this.resId = resId;
        this.path = path;
        this.maxFace = maxFace;
        this.effectType = effectType;
        this.description = description;
    }

    public String bundleName() {
        return bundleName;
    }

    public int resId() {
        return resId;
    }

    public String path() {
        return path;
    }

    public int maxFace() {
        return maxFace;
    }

    public int effectType() {
        return effectType;
    }

    public String description() {
        return description;
    }

    public Effect effect() {
        return new Effect(bundleName, resId, path, maxFace, effectType, description);
    }

    public static ArrayList<Effect> getEffectsByEffectType(int effectType) {
        ArrayList<Effect> effects = new ArrayList<>();
        effects.add(EffectNone.effect());
        for (EffectEnum e : EffectEnum.values()) {
            if (e.effectType == effectType) {
                effects.add(e.effect());
            }
        }
        return effects;
    }
}
