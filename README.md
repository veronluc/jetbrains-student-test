# Jetbrains student test - Luc Véron

Launch steps: 
* Assume you have java and maven installed on your computer
* Clone the GitHub project
* Open the project with IntelliJ IDEA
* Create a maven build profile with the following parameters : 
    * working directory: folder where you cloned the project
    * cli: clean javafx:run
    
***    
Ideas to technically improve the software : 
* Add unit tests on functions, for example concerning the text highlighting feature
* Add actions in order to process pipelines when pull requesting on the GitHub repository : tests, checkstyle, sonar, etc.
* Use several controllers instead of one in order the code maintainability 
* Use the kotlin-main-kts library in order to include Kotlin compilation inside the IDE
* Avoid using Scene Builder to create the interface (I used it to develop it faster)