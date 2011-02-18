@cls
@echo off
@set path=%path%;"c:\program files\java\bin"
@set classpath=%classpath%;.;./obj;./bin
@java ar.com.kernelfusion.icsuda.client.ICSUDA_C
