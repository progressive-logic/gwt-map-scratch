gwt-map-scratch
===============

Experiments with Google Web Toolkit and Google Maps 3.8 alpha


Getting Started
---------------

* Use Eclipse - http://www.eclipse.org/
* Install GWT SDK development environment - http://www.gwtproject.org/
* Create a new "Google Web Application" project
  - don't select "Use Google App Engine"
  - don't select "Generate sample project code"
* Make this git repo. into that project
* Copy Google Maps 3 API Jar into the .../libs/ directory 
  Better to use a google supplied binding-
  https://code.google.com/p/gwt-google-apis/downloads/detail?name=gwt-maps-3.8.0-pre1.zip
  and "Add JARs..." to the project's "Java Build Path" -> "Libraries"
* Slide bar widget is from here- https://code.google.com/p/gwt-slider-bar/
* Marker ico pre-loading with this - https://code.google.com/p/gwt-image-loader/
* Other Slide bar - https://code.google.com/p/google-web-toolkit-incubator/wiki/Downloads?tm=2
* Charts 0.9.10 https://code.google.com/p/gwt-charts/wiki/Downloads

Compile / Debug Problems
------------------------
* Try -Xss5M to increase the stack size


Attribution
-------------
* Base CSS - http://getbootstrap.com/
* Map Marker Icons http://mapicons.nicolasmollet.com/
