# AEM Adobe Learning Manager Component

Adobe Learning Manager component for Adobe Experience Manager (AEM) to embed various widgets present in the Adobe Learning Manager learner app in AEM sites. Please refer this [helpx page](https://helpx.adobe.com/captivate-prime/integrate-aem-captivate-prime.html)

## Released Builds/Packages
AEM package for the component 

* [`learning-manager.all-x.x.x.zip`](https://github.com/adobe/captivate-prime-aem-components/releases/latest): Build for AEM as a Cloud Service
* [`learning-manager.all-x.x.x-classic.zip`](https://github.com/adobe/captivate-prime-aem-components/releases/latest): Package for AEM 6.4+, AEM 6.5+


## How to build

To build all the modules for **AEM as a Cloud Service** run in the project root directory the following command with Maven 3:

    mvn clean install

Project is also compatible with AEM **6.4+** and **6.5+**. Add profile `classic` when executing a build, i.e.:

    mvn clean install -Pclassic

zip package- **learning-manager.all-x.x.x.zip** will be available in `all/target` 	 folder.


## System Requirements

AEM as a Cloud Service | AEM 6.5 | AEM 6.4 | Java SE | Maven
-----------------------|---------|---------|---------|------
Continual | 6.5+ (*) | 6.4+ (*) | 8, 11 | 3.3.9+


## Testing

* There are unit tests of the code in core i.e. bundle. To test, execute:

    ```
    mvn clean test
    ```

## Maven settings

The project comes with the auto-public repository configured. To setup the repository in your Maven settings, refer to:

    http://helpx.adobe.com/experience-manager/kb/SetUpTheAdobeMavenRepository.html
