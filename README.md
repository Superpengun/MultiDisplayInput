# MultiDisplayInput
MultiClientInputMethod for Android(Multi-session/Multi-screen input method)<br>
更多交流加QQ：3323181861
研究了很久的双屏输入法，终于搞出来个可用的demo<br>
More communication with QQ: 3323181861 . After studying the multi-screen inputmethod for a long time, I finally came up with a usable demo<br>
设置该输入法调起前需要按步骤执行命令哦<br>
You need to follow the steps to execute the command before setting up the input method.<br>
```
adb shell setprop persist.debug.multi_client_ime com.zqy.multidisplayinput/.MultiClientInputMethod
adb reboot
```
相信这个你是第一次看到能让你调起来的多屏输入法的教程，那么给我右上角打一颗星星吧谢谢了<br>
I believe this is the first time you have seen a multi-screen inputmethod that allows you to adjust, please give me a star in the upper right corner, thank you<br>
![image](https://s3.bmp.ovh/imgs/2022/05/06/497c56612354df14.png)<br>
同时在最近的交流中发现，在单物理屏幕上使用虚拟屏扩展出的屏幕也是一种应用场景（如折叠屏）
![image](https://i.postimg.cc/02kVW8cj/image.png)<br>
注意：如果你是在api31以下的系统使用，那么要替换framework jar包为api30的，否则有崩溃问题！
