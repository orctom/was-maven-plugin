import sys
import getopt
import time
import traceback

host = r"{{host}}"
cell = r"{{cell}}"
cluster = r"{{cluster}}"
server = r"{{server}}"
node = r"{{node}}"
applicationName = r"{{applicationName}}"
contextRoot = r"{{contextRoot}}"
virtualHost = r"{{virtualHost}}"
sharedLibs = r"{{sharedLibs}}"
parentLast = r"{{parentLast}}"
webModuleParentLast = r"{{webModuleParentLast}}"
packageFile = r"{{packageFile}}"
restartAfterDeploy = r"{{restartAfterDeploy}}"


class WebSphere:
    def listApplications(self):
        print "[LIST APPLICATIONS]", host
        print time.strftime("%Y-%b-%d %H:%M:%S %Z")
        print AdminApp.list()

    def restartServer(self):
        print '-' * 60
        print "[RESTARTING SERVER]", host
        print time.strftime("%Y-%b-%d %H:%M:%S %Z")
        print '-' * 60
        try:
            if "" != cluster:
                appManager = AdminControl.queryNames('name=' + cluster + ',type=Cluster,process=dmgr,*')
                print AdminControl.invoke(appManager, 'rippleStart')
                print ""
                print '=' * 60
                print "[NOTICE]: It takes a few minutes for the cluster to fully back running after the build finished!"
                print '=' * 60
                print ""
            elif "" != node:
                appManager = AdminControl.queryNames('node=' + node + ',type=Server,process=' + server + ',*')
                print AdminControl.invoke(appManager, 'restart')
            else:
                appManager = AdminControl.queryNames('type=Server,process=' + server + ',*')
                print AdminControl.invoke(appManager, 'restart')
        except:
            print "FAILED to restart cluster/server:"
            print '-' * 10
            traceback.print_exc(file=sys.stdout)
            print '-' * 10
            print "try to startApplication directly..."
            self.startApplication()

    def startApplication(self):
        print '-' * 60
        print "[STARTING APPLICATION]", host, applicationName
        print time.strftime("%Y-%b-%d %H:%M:%S %Z")
        print '-' * 60
        try:
            if "" != cluster:
                cell = AdminControl.getCell()
                managedNodeNames = AdminTask.listManagedNodes().splitlines()
                for managedNodeName in managedNodeNames:
                    appManager = AdminControl.queryNames(
                        'type=ApplicationManager,process=' + server + ',node=' + managedNodeName + ',cell=' + cell + ',*')
                    print "Starting " + applicationName + " on " + managedNodeName
            elif "" != node:
                appManager = AdminControl.queryNames('node=' + node + ',type=Server,process=' + server + ',*')
            else:
                appManager = AdminControl.queryNames('type=Server,process=' + server + ',*')
            print AdminControl.invoke(appManager, 'startApplication', applicationName)
            # AdminApplication.startApplicationOnCluster(applicationName, cluster)
        except:
            print "FAILED to start application:"
            print '-' * 10
            traceback.print_exc(file=sys.stdout)
            print '-' * 10
            raise

    def stopApplication(self):
        print '-' * 60
        print "[STOPPING APPLICATION]", host, applicationName
        print time.strftime("%Y-%b-%d %H:%M:%S %Z")
        print '-' * 60
        try:
            if "" != cluster:
                cell = AdminControl.getCell()
                managedNodeNames = AdminTask.listManagedNodes().splitlines()
                for managedNodeName in managedNodeNames:
                    appManager = AdminControl.queryNames(
                        'type=ApplicationManager,process=' + server + ',node=' + managedNodeName + ',cell=' + cell + ',*')
                    print "Starting " + applicationName + " on " + managedNodeName
            elif "" != node:
                appManager = AdminControl.queryNames('node=' + node + ',type=Server,process=' + server + ',*')
            else:
                appManager = AdminControl.queryNames('type=Server,process=' + server + ',*')
            print AdminControl.invoke(appManager, 'stopApplication', applicationName)
            # AdminApplication.stopApplicationOnCluster(applicationName, cluster)
        except:
            print "FAILED to stop application:"
            print '-' * 10
            traceback.print_exc(file=sys.stdout)
            print '-' * 10
            raise

    def installApplication(self):
        print '-' * 60
        print "[INSTALLING APPLICATION]", host, applicationName
        print time.strftime("%Y-%b-%d %H:%M:%S %Z")
        print '-' * 60

        options = ['-deployws', '-distributeApp', '-appname', applicationName]

        try:
            if "" != cluster:
                cell = AdminControl.getCell()
                serverMapping = 'WebSphere:cell=' + cell + ',cluster=' + cluster
                unmanagedNodeNames = AdminTask.listUnmanagedNodes().splitlines()
                for unmanagedNodeName in unmanagedNodeNames:
                    webservers = AdminTask.listServers('[-serverType WEB_SERVER -nodeName ' + unmanagedNodeName + ']').splitlines()
                    for webserver in webservers:
                        webserverName = AdminConfig.showAttribute(webserver, 'name')
                        serverMapping = serverMapping + '+WebSphere:cell=' + cell + ',node=' + unmanagedNodeName + ',server=' + webserverName
                options += ['-cluster', cluster, '-MapModulesToServers', [['.*', '.*', serverMapping]]]
            else:
                serverMapping = 'WebSphere:server=' + server
                options += ['-MapModulesToServers', [['.*', '.*', serverMapping]]]

            if "" != contextRoot:
                options += ['-contextroot', contextRoot]

            if "" != virtualHost:
                options += ['-MapWebModToVH', [['.*', '.*', virtualHost]]]

            if "" != sharedLibs:
                libs = []
                for lib in sharedLibs.split(','):
                    libs.append(['.*', '.*', lib])
                options += ['-MapSharedLibForMod', libs]

            print ""
            print "options: ", options
            print ""

            print "INSTALLING"
            print AdminApp.install(packageFile, options)

            self.__modifyClassloader()

            print "SAVING CONFIG"
            AdminConfig.save()

            if "" != cluster:
                print "SYNCING"
                AdminNodeManagement.syncActiveNodes()

            result = AdminApp.isAppReady(applicationName)
            while result == "false":
                print "STATUS:", AdminApp.getDeployStatus(applicationName)
                time.sleep(5)
                result = AdminApp.isAppReady(applicationName)
            print "INSTALLED", applicationName
        except:
            print "FAILED to install application: ", applicationName
            print '-' * 10
            traceback.print_exc(file=sys.stdout)
            print '-' * 10
            raise

    def __modifyClassloader(self):
        deployments = AdminConfig.getid("/Deployment:" + applicationName + "/")
        deploymentObject = AdminConfig.showAttribute(deployments, "deployedObject")

        if "true" == parentLast:
            # AdminApplication.configureClassLoaderLoadingModeForAnApplication(applicationName, "PARENT_LAST")
            classloader = AdminConfig.showAttribute(deploymentObject, "classloader")
            AdminConfig.modify(classloader, [['mode', 'PARENT_LAST']])

        if "true" == webModuleParentLast:
            modules = AdminConfig.showAttribute(deploymentObject, "modules")
            arrayModules = modules[1:len(modules) - 1].split(" ")
            for module in arrayModules:
                if module.find('WebModuleDeployment') != -1:
                    AdminConfig.modify(module, [['classloaderMode', 'PARENT_LAST']])

    def uninstallApplication(self):
        print '-' * 60
        print "[UNINSTALLING APPLICATION]", host, applicationName
        print time.strftime("%Y-%b-%d %H:%M:%S %Z")
        print '-' * 60
        try:
            print AdminApp.uninstall(applicationName)
            AdminConfig.save()
            if "" != cluster:
                AdminNodeManagement.syncActiveNodes()
        except:
            print "FAILED to uninstall application: ", applicationName
            print '-' * 10
            traceback.print_exc(file=sys.stdout)
            print '-' * 10
            raise

    def isApplicationInstalled(self):
        return AdminApplication.checkIfAppExists(applicationName)

    def deploy(self):
        if "true" == self.isApplicationInstalled():
            self.uninstallApplication()

        self.installApplication()
        if "true" == restartAfterDeploy:
            self.restartServer()
        else:
            print "Starting application instead of restarting server..."
            self.startApplication()

        print '-' * 60
        print "[FINISHED]", host, applicationName
        print time.strftime("%Y-%b-%d %H:%M:%S %Z")
        print '-' * 60

# -----------------------------------------------------------------
# Main
# -----------------------------------------------------------------

if __name__ == "__main__":
    methods, args = getopt.getopt(sys.argv, 'o:')
    for name, method in methods:
        if name == "-o":
            getattr(WebSphere(), method)()
