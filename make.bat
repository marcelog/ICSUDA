@cls
@echo off
@del /q /s .\obj\* > nul
@set path=%path%;"c:/program files/java/bin"
@set classpath=%classpath%;.;./obj
@call clean.bat
@javac -d ./obj ar/com/kernelfusion/icsuda/server/ICSUDA_S.java
@javac -d ./obj ar/com/kernelfusion/icsuda/client/ICSUDA_C.java
rem @javac -d ./obj ar/com/kernelfusion/icsuda/applet/ICSUDA_A.java