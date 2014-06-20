# was-maven-plugin [![Build Status](https://api.travis-ci.org/orctom/was-maven-plugin.png)](https://travis-ci.org/orctom/was-maven-plugin)

- [Introduction](#introduction)
- [Goal-`deploy`](#goal-deploy)
	- [Parameters](#parameters)
- [Single Target Server](#single-target-server)
- [Multi Target Servers](#multi-target-servers)
- [Pre-Steps and Post-Steps](#pre-steps-and-post-steps)
- [Customized Jython Script File](#customized-jython-script-file)
- [Continues Deployment with Jenkins](#continues-deployment-with-jenkins)
- [With Global Security Turned on](#with-global-security-turned-on)
- [Change List](#change-list)
	- [Next Release](#next-release)
	- [1.0.7](#107)
	- [1.0.6](#106)
	- [1.0.5](#105)
	- [1.0.4](#104)
	- [1.0.3](#103)

## Introduction

Maven plugin to deploy a single war or ear to one or multi local or remote WebSphere Application Server (WAS) at a single build.  
Tested on WAS 8.5  
**NOTE: WebSphere Application Server installation required on host box!**

## Goal-`deploy`
The only goal of this plugin, it will:
 1. Check if an application with the same name already installed on target server(s)/cluster(s)
 	* Uninstall it if yes
 2. Install the package to target server(s)/cluster(s)
 3. Restart target server(s)/cluster(s)

### Parameters
| Name						| Type		| Description																								|
| ------------------------- | --------- | --------------------------------------------------------------------------------------------------------- |
| **wasHome**				| String	| WebSphere Application Server home. Default: `${env.WAS_HOME}`, **required**								|
| **applicationName**		| String	| Application name displayed in admin console. Default: `${project.build.finalName}`						|
| applicationNameSuffix		| String	| Suffix will be appended to applicationName, as `applicationName_applicationNameSuffix`					|
| host						| String	| Local/Remote WAS IP/domain URL. e.g. `10.95.0.100`, `devtrunk01.company.com`, default: `localhost`   		|
| port						| String	| Default: `8879` (when `cluster` not empty); `8880` (when `cluster` empty)									|
| connectorType 			| String	| Default: `SOAP` 																							|
| cluster					| String	| Target cluster name, **required** if target WAS is a cluster	    										|
| cell						| String	| Target cell name																							|
| node						| String	| Target node name, **required** if target WAS is NOT a cluster 											|
| server					| String	| Target server name, **required**																			|
| virtualHost				| String	| Target virtual host name																					|
| user						| String	| Account username for **target WAS** admin console, if global security is turned on						|
| password					| String	| Account password for **target WAS** admin console, if global security is turned on						|
| contextRoot				| String	| **required** for war deployment                   														|
| sharedLibs				| String	| Bind the exist shared libs to ear/war, comma-separated (,)												|
| parentLast				| Boolean	| `true` to set classloader mode of application to `PARENT_LAST`, default `false`							|
| webModuleParentLast		| Boolean	| `true` to set classloader mode of web module to `PARENT_LAST`, default `false`							|
| **packageFile**			| String	| The EAR/WAR package that will be deployed to remote RAS, Default: `${project.artifact.file}`				|
| **failOnError**			| Boolean	| Default: `false` Whether failed the build when failed to deploy.                          				|
| **verbose**				| Boolean	| Whether show more detailed info in log																	|
| **script**				| String	| Your own jython script for deployment. Double braces for variables, such as: `{{cluster}}`                |
| **scriptArgs**			| String	| Args that will be passed to the `script`                                          	                    |
| **preSteps**				| Ant tasks	| Ant tasks that can be executed before the deployments														|
| **postSteps**				| Ant tasks	| Ant tasks that can be executed after the deployments														|
| deploymentsPropertyFile	| File		| For multi target, hold above parameters, except those in **bold**. Default: `was-maven-plugin.properties`	|

Generally, you need to specify at least
 * `claster` and `server` for a cluster
 * `server` and `node` for a non-cluster

## Single Target Server
```xml
<plugin>
	<groupId>com.orctom.mojo</groupId>
	<artifactId>was-maven-plugin</artifactId>
	<version>1.0.7</version>
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

## Multi Target Servers
#### was-maven-plugin.properties
This property file contains the meta config for target WAS.  
The section name will be used to identify each target WAS.

**Please put `was-maven-plugin.properties` to the same folder as `pom.xml`, to make it available as `${project.basedir}/was-maven-plugin.properties`**

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
	<version>1.0.7</version>
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
**Deploy to dev-trunk1 and dev-trunk2**
```
mvn clean install -Ddeploy_targets=dev-trunk1,dev-trunk2
```
**Deploy to dev-trunk2 and dev-trunk3**
```
mvn clean install -Ddeploy_targets=dev-trunk2,dev-trunk3
```

## Pre-Steps and Post-Steps
```xml
<plugin>
	<groupId>com.orctom.mojo</groupId>
	<artifactId>was-maven-plugin</artifactId>
	<version>1.0.7</version>
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

## Customized Jython Script File
If you'd like to go with a customized jython script file for deployment.

Double braces for variables, such as: `{{cluster}}`, properties in was-maven-plugin.properties are all available as variables.
```xml
<plugin>
	<groupId>com.orctom.mojo</groupId>
	<artifactId>was-maven-plugin</artifactId>
	<version>1.0.7</version>
	<executions>
		<execution>
			<id>deploy</id>
			<phase>install</phase>
			<goals>
				<goal>deploy</goal>
			</goals>
			<configuration>
				<wasHome>${env.WAS_HOME}</wasHome>
				<script>your-jython-script.py</script><!-- "/xxx" for absolute path; "xxx" for ${basedir}/xxx -->
				<scriptArgs>optional-args</scriptArgs>
                <verbose>true</verbose>
			</configuration>
		</execution>
	</executions>
</plugin>
```

## Continues Deployment with Jenkins
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
					<version>1.0.7</version>
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
**Configure**

![Jenkins Job configure](https://raw.github.com/orctom/was-maven-plugin/master/screenshots/configure.png "Jenkins Job Configure")

**Trigger**

![Jenkins Job Trigger](https://raw.github.com/orctom/was-maven-plugin/master/screenshots/trigger.png "Jenkins Job Trigger")

## With Global Security Turned on
When Global Security is enabled on remote WAS (not under a same deployment manager), certificates of remote WAS need to be added to local trust store. 
We could configure WAS to prompt to add them to local trust store.
 1. Open ${WAS_HOME}/properties/ssl.client.props 
 2. Change the value of `com.ibm.ssl.enableSignerExchangePrompt` to `gui` or `stdin`

* `gui`: will prompt a Java based window, this requires a X window installed. 
* `stdin`: when using ssh, or on client linux without X window installed. 

## Change List

#### Next Release
- [x] Not checking whether parent folder of script been created and raising exception if not.

#### 1.0.7
* removed `preCompileJSPs` options for deployment

#### 1.0.6
* Fixed multi-server deployment issue

#### 1.0.5
* Downgraded to use 1.5 build level
* Fixed property resolving issue, properties in was-maven-plugin.properties are all available in custome scripts and pre/post steps

#### 1.0.4
* Added `PARENT_LAST` for application and web module and `sahred libs` bindings
* Added `failonerror`

#### 1.0.3
* Removed private project specific logic. (it's the 1st working version for general projects for websphere deployment)
