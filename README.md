# packetSniffer
<header>
  <p> <b>  app-level build.gradle file</b>

dependencies {
    implementation 'org.pcap4j:pcap4j-core:1.8.0'
    implementation 'org.pcap4j:pcap4j-packetfactory-static:1.8.0'
    implementation 'org.pcap4j:pcap4j-packetfactory-propertiesbased:1.8.0'
}

</p>
</header>
<header>
  
  <p>
    This has not been tested. MainActivity needs to be cleaned up. None of the outside classes are needed but should provide insight into how the other import classes work. Follow the instructions below to import RemoteControlManager from the SDK import for Samsung devices. 
    <div><b> 
      app's build.gradle file 
    </b> </div>
    
    implementation files('libs/smartview.jar')
implementation 'org.eclipse.jetty:jetty-client:9.4.6.v20170531'
implementation 'org.eclipse.jetty.websocket:websocket-client:9.4.6.v20170531'
implementation 'org.json:json:20140107'
  </p>
  </header>
  
<div>
 <header><b>AndroidManifest.xml file</b></header> </div>
 <header1>
   <p>
<div>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
</div>
  </p>
</header1>

