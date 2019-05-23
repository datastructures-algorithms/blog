SET JAVA_HOME=c:\Java\jdk1.8.0_181_copy 
cl ArrayListAnalyzeAndTime.c /O1 /I%JAVA_HOME%\include\ /I%JAVA_HOME%\include\win32 /LD /link /NODEFAULTLIB /ENTRY:DllEntryPoint
