# kubernetes-hpa-loadtest
Kubernetes Horizontal Pod Autoscaling(HPA) Load Test Client

-  Prereq: Java 18 or higher, IntelliJ/Eclipse
-  Spring Reactive WebClient - K8sLoadTestClient.java
     - JVM options for K8sLoadTestClient.java: -Xmx2g -Xmn2g -Xss500m
     - 20 Threads in Thread pool 
     - 200K Iterations (Parallel Http Requests)
-  Screen shot of the pods scale and shrink automatic

![img.png](img.png)   
