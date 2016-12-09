# u1PluginFramework
android 插件框架  
动态加载apk   
解决插件间的依赖  
##1.实现原理：
  实现原理参考了dynamic_apk_loader，和这个框架一样，通过代理的方式启动activity和service
##2.支持以下功能：
  （1）.动态加载apk，不需要运行即可运行，每个apk就是一个插件  
  （2）.可用同配置文件plugin.xml声明一个插件依赖的其他插件，声明以后就可以使用它所以赖的插件的代码和资源。例如声明plugin A依赖Plugin B，则在
    plugin A中可以使用plugin B中的资源和代码  
  （3）.任何安装的插件都可以使用宿主的资源和代码  
    
  Tip：此框架还有好多不足之处，欢迎有兴趣的朋友指正和优化
