@cls
@echo off
@set path=%path%;"c:\program files\java\bin"
@set classpath=%classpath%;.;./obj;./bin/jconnector-2.0.14.jar
@java ar.com.kernelfusion.icsuda.server.ICSUDA_S %1 %2
