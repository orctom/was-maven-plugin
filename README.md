# was-maven-plugin [![Build Status](https://api.travis-ci.org/orctom/was-maven-plugin.png)](https://travis-ci.org/orctom/was-maven-plugin)

Maven plugin to deploy single artifact to one or multi local/remote WebSphere Application Server (WAS) 8.5

## Usage
mvn was-maven-plugin:deploy

**NOTE: 1.0.1 and 1.0.2 is not working, please don't use them!**

## Parameters
| Name						| Type		| Description																								|
| ------------------------- | --------- | --------------------------------------------------------------------------------------------------------- |
| **wasHome**				| String	| WebSphere Application Server home. Default: `${env.WAS_HOME}`, `required`									|
| **applicationName**		| String	| Application name displayed in admin console. Default: ${project.build.finalName}`							|
| applicationNameSuffix		| String	| Suffix will be appended to applicationName, as `applicationName_applicationNameSuffix`					|
| host						| String	| Local/Remote WAS IP/domain URL. e.g. `localhost`, `10.95.197.181`, `devtrunk01.company.com`, `required`	|
| port						| String	| Default: `8879` (when `cluster` not empty); `8880` (when `cluster` empty)									|
| connectorType 			| String	| Default: `SOAP` 																							|
| cluster					| String	| Target cluster name, `required` if target is was is a cluster												|
| cell						| String	| Target cell name																							|
| node						| String	| Target node name, `required` if target is was is NOT a cluster											|
| server					| String	| Target server name, `required`																			|
| virtualHost				| String	| Target virtual host name																					|
| user						| String	| Account user name for WAS admin console																	|
| password					| String	| Account password for WAS admin console																	|
| contextRoot				| String	| Context Path if it's a war,`required` if t's a war														|
| **packageFile**			| String	| The EAR/WAR package that will be deployed to remote RAS, Default: `${project.artifact.file}`				|
| **failOnError**			| Boolean	| Default: `false` Whether failed the build when failed to deploy. **true not supported yet**				|
| **verbose**				| Boolean	| Whether show more detailed info in log																	|
| **mode**					| String	| Approach to do the deployment: `SCRIPT` (jython script), `ANT` (WebSphere ant tasks). Default: `SCRIPT`	|
| **preSteps**				| Ant tasks	| Ant tasks that can be executed before the deployments														|
| **postSteps**				| Ant tasks	| Ant tasks that can be executed after the deployments														|
| deploymentsPropertyFile	| File		| For multi target, lold above parameters, except those in **bold**. Default: `was-maven-plugin.properties`.|

### Single Target Server
```xml
<plugin>
	<groupId>com.orctom.mojo</groupId>
	<artifactId>was-maven-plugin</artifactId>
	<version>1.0.3</version>
	<executions>
		<execution>
			<id>deploy</id>
			<phase>install</phase>
			<goals>
				<goal>deploy</goal>
			</goals>
			<configuration>
				<wasHome>${env.WAS_HOME}</wasHome>
				<applicationName>${project.build.finalName}</applicationName>
				<host>localhost</host>
				<server>server01</server>
				<node>node01</node>
				<virtualHost>default_host</virtualHost>
                <verbose>true</verbose>
			</configuration>
		</execution>
	</executions>
</plugin>
```

### Multi Target Servers
#### was-maven-plugin.properties
**put it to /src/main/resources/ to make sure it's available as `${project.basedir}/was-maven-plugin.properties`**
```properties
[DEFAULT]
virtualHost=default_host

[dev-trunk1]
host=devtrunk1.company.com
applicationNameSuffix=trunk1
cluster=cluster01
server=server01

[dev-trunk2]
host=devtrunk2.company.com
applicationNameSuffix=trunk2
cluster=cluster02
server=server02

[dev-trunk3]
host=devtrunk3.company.com
applicationNameSuffix=trunk3
cluster=cluster03
server=server03
virtualHost=devtrunk3_host
```

#### pom.xml
```xml
<plugin>
	<groupId>com.orctom.mojo</groupId>
	<artifactId>was-maven-plugin</artifactId>
	<version>1.0.3</version>
	<executions>
		<execution>
			<id>deploy</id>
			<phase>install</phase>
			<goals>
				<goal>deploy</goal>
			</goals>
			<configuration>
				<wasHome>${env.WAS_HOME}</wasHome>
                <verbose>true</verbose>
			</configuration>
		</execution>
	</executions>
</plugin>
```
#### Deploy to dev-trunk1 and dev-trunk2
```
mvn clean install -Ddeploy_targets=dev-trunk1,dev-trunk2
```
#### Deploy to dev-trunk2 and dev-trunk3
```
mvn clean install -Ddeploy_targets=dev-trunk2,dev-trunk3
```

### Pre-Steps and Post-Steps
```xml
<plugin>
	<groupId>com.orctom.mojo</groupId>
	<artifactId>was-maven-plugin</artifactId>
	<version>1.0.3</version>
	<executions>
		<execution>
			<id>deploy</id>
			<phase>install</phase>
			<goals>
				<goal>deploy</goal>
			</goals>
			<configuration>
				<wasHome>${env.WAS_HOME}</wasHome>
                <verbose>true</verbose>
				<preSteps>
					<target name="pre 1">
						<echo message="====== pre 1 ===== ${applicationName}" />
					</target>
					<target name="pre 2">
						<echo message="====== pre 2 =====" />
					</target>
				</preSteps>
				<postSteps>
					<target name="post 1">
						<echo message="====== post 1 =====" />
					</target>
					<target name="post 2">
						<echo message="====== post 2 =====" />
						<sleep seconds="10"/>
					</target>
				</postSteps>
			</configuration>
		</execution>
	</executions>
	<dependencies>
		<dependency>
			<groupId>ant-contrib</groupId>
			<artifactId>ant-contrib</artifactId>
			<version>20020829</version>
		</dependency>
		<dependency>
			<groupId>org.apache.ant</groupId>
			<artifactId>ant-jsch</artifactId>
			<version>1.8.4</version>
		</dependency>
		<dependency>
			<groupId>com.jcraft</groupId>
			<artifactId>jsch</artifactId>
			<version>0.1.49</version>
		</dependency>
	</dependencies>
</plugin>
```
* **pre-steps/post-steps can be used with both single target server and multi target servers**
* **All properties defined in properties section of pom or in was-maven-plugin.properties are available in pre-steps/post-steps ant tasks**

### Continues Deployment with Jenkins
We could move this plugin to a profile, and utilize [Extended Choice Parameter plugin](https://wiki.jenkins-ci.org/display/JENKINS/Extended+Choice+Parameter+plugin) to make this parameterized.

#### Sample pom.xml
```xml
<profiles>
	<profile>
		<id>deploy</id>
		<activation>
			<property>
				<name>deploy</name>
				<value>true</value>
			</property>
		</activation>
		<build>
			<plugins>
				<plugin>
					<groupId>com.orctom.mojo</groupId>
					<artifactId>was-maven-plugin</artifactId>
					<version>1.0.3</version>
					<executions>
						<execution>
							<id>deploy</id>
							<phase>install</phase>
							<goals>
								<goal>deploy</goal>
							</goals>
							<configuration>
								<wasHome>${env.WAS_HOME}</wasHome>
								<verbose>true</verbose>
								<preSteps>
									<target name="unzip-Config-zip">
										<echo message="Unzipping ${project.build.directory}/Config.zip --> WAS shared libs folder" />
										<unzip dest="${WAS shared libs folder}/conf">
											<fileset dir="${project.build.directory}/">
												<include name="Config.zip" />
											</fileset>
										</unzip>
									</target>
									<target name="unzip-static-zip">
										<taskdef resource="net/sf/antcontrib/antcontrib.properties" />
										<if>
											<available file="${project.build.directory}/static.zip" />
											<then>
												<echo message="Unzipping ${project.build.directory}/static.zip --> apache sratic path" />
												<unzip dest="${apache sratic path}" src="${project.build.directory}/static.zip" />
											</then>
										</if>
									</target>
									<target name="copy-config-to-remote">
										<taskdef resource="net/sf/antcontrib/antcontrib.properties" />
										<if>
											<isset property="some property name in pom or was-maven-plugin/properties" />
											<then>
												<echo message="Coping ${WAS shared libs folder}/conf to ${remote ip}:${WAS shared libs folder}/conf ..." />
												<scp todir="wsadmin@${remote ip}:${WAS shared libs folder}/conf" keyfile="${user.home}/.ssh/id_rsa" trust="true" failonerror="false">
													<fileset dir="${WAS shared libs folder}/conf" />
												</scp>
												<echo message="Copied ${meta.config.path}/conf" />
											</then>
											<else>
												<echo message="Skipped, not needed." />
											</else>
										</if>
									</target>
								</preSteps>
							</configuration>
						</execution>
					</executions>
					<dependencies>
						<dependency>
							<groupId>ant-contrib</groupId>
							<artifactId>ant-contrib</artifactId>
							<version>20020829</version>
						</dependency>
						<dependency>
							<groupId>org.apache.ant</groupId>
							<artifactId>ant-jsch</artifactId>
							<version>1.8.4</version>
						</dependency>
						<dependency>
							<groupId>com.jcraft</groupId>
							<artifactId>jsch</artifactId>
							<version>0.1.49</version>
						</dependency>
					</dependencies>
				</plugin>
			</plugins>
		</build>
	</profile>
</profiles>
```
#### Sample Jenkins Job Configuration
##### Configure
![Jenkins Job configure](https://raw.github.com/orctom/was-maven-plugin/master/screenshots/configure.png "Jenkins Job Configure")
##### Trigger
![Jenkins Job Trigger](https://raw.github.com/orctom/was-maven-plugin/master/screenshots/trigger.png "Jenkins Job Trigger")

### With Global Security Turned on
When Global Security is enabled on remote WAS, certificates of remote WAS need to be added to local trust store. We could configure WAS to prompt to add them to local trust store.
 1. Open ${WAS_HOME}/properties/ssl.client.props 
 2. Change the value of `com.ibm.ssl.enableSignerExchangePrompt` to `gui` or `stdin`

* `gui`: will prompt a Java based window, this requires a X window installed. 
* `stdin`: when using ssh, or on client linux without X window installed. 
