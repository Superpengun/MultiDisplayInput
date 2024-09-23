package com.parking.keyboard.engine;

/**
 * 港澳车牌键位限定：
 * 当第一位是"粤"时，"港澳"按键才可点击。
 */
public class HKMacaoKeyTransformer extends LayoutMixer.AbstractTypedKeyTransformer {

    public HKMacaoKeyTransformer() {
        super(KeyType.GENERAL);
    }

    @Override
    protected KeyEntry transform(Context ctx, KeyEntry key) {
        if (6 == ctx.selectIndex && "港澳".contains(key.text)) {
            return KeyEntry.newOfSetEnable(key, ctx.presetNumber.startsWith("粤"));
        } else if (0 == ctx.selectIndex && !"粤".contains(key.text) && ctx.presetNumber.length()>=6){
            return KeyEntry.newOfSetEnable(key, !(ctx.presetNumber.endsWith("港") || ctx.presetNumber.endsWith("澳")));
        }
        return key;
    }
}
