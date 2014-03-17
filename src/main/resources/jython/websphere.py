import sys
import getopt
import time

cell = r"{{cell}}"
cluster = r"{{cluster}}"
server = r"{{server}}"
node = r"{{node}}"
applicationName = r"{{applicationName}}"
contextRoot = r"{{contextRoot}}"
virtualHost = r"{{virtualHost}}"
packageFile = r"{{packageFile}}"


class WebSphere:
    def listApplications(self):
        print "list applications"
        print AdminApp.list()
        return AdminApplication.listApplications()

    def restartServer(self):
        print "restarting server"
        if "" != cluster:
            print AdminTask.updateAppOnCluster('[-ApplicationNames ' + applicationName + '}} -timeout 3600]')
        else:
            print AdminControl.stopServer(server, node)
            print AdminControl.startServer(server, node)

    def startApplication(self):
        print "starting application"
        if "" == node:
            appManager = AdminControl.queryNames('type=ApplicationManager,process=' + server + ',*')
        else:
            appManager = AdminControl.queryNames('node=' + node + ',type=ApplicationManager,process=' + server + ',*')
        print appManager
        AdminControl.invoke(appManager, 'startApplication', applicationName)
        #AdminApplication.startApplicationOnCluster(applicationName, cluster)

    def stopApplication(self):
        print "stopping application"
        if "" == node:
            appManager = AdminControl.queryNames('type=ApplicationManager,process=' + server + ',*')
        else:
            appManager = AdminControl.queryNames('node=' + node + ',type=ApplicationManager,process=' + server + ',*')
        print appManager
        AdminControl.invoke(appManager, 'stopApplication', applicationName)
        #AdminApplication.stopApplicationOnCluster(applicationName, cluster)

    def installApplication(self):
        try:
            print "installing application:", applicationName
            if "" != cluster:
                serverMapping = 'WebSphere:cluster=' + cluster
                options = ['-deployws', '-distributeApp', '-appname', applicationName, '-cluster', cluster, '-server', server, '-MapModulesToServers', [['.*','.*', serverMapping]], '-MapWebModToVH', [['.*','.*', virtualHost]]]
            elif "" != contextRoot:
                serverMapping = 'WebSphere:server=' + server
                options = ['-distributeApp', '-appname', applicationName, '-contextroot', contextRoot, '-server', server, '-MapModulesToServers', [['.*','.*', serverMapping]], '-MapWebModToVH', [['.*','.*', virtualHost]]]
            else:
                serverMapping = 'WebSphere:server=' + server
                options = ['-distributeApp', '-appname', applicationName, '-server', server, '-MapModulesToServers', [['.*','.*', serverMapping]], '-MapWebModToVH', [['.*','.*', virtualHost]]]

            print "installing"
            AdminApp.install(packageFile, options)

            print "saving config"
            AdminConfig.save()

            print "syncing"
            AdminNodeManagement.syncActiveNodes()

            result = AdminApp.isAppReady(applicationName)
            while result == "false":
                print "status:", AdminApp.getDeployStatus(applicationName)
                time.sleep(5)
                result = AdminApp.isAppReady(applicationName)
            print "installed", applicationName
        except:
            exc_type, exc_value, exc_traceback = sys.exc_info()
            lines = traceback.format_exception(exc_type, exc_value, exc_traceback)
            print "Exception happened:", lines

    def uninstallApplication(self):
        print "uninstalling application:", applicationName
        AdminApp.uninstall(applicationName)
        AdminConfig.save()
        AdminNodeManagement.syncActiveNodes()

    def isApplicationInstalled(self):
        return AdminApplication.checkIfAppExists(applicationName)

    def deploy(self):
        if "true" == self.isApplicationInstalled():
            self.uninstallApplication()

        self.installApplication()
        self.restartServer()


#-----------------------------------------------------------------
# Main
#-----------------------------------------------------------------

if __name__ == "__main__":
    methods, args = getopt.getopt(sys.argv, 'o:')
    for name, method in methods:
        if name == "-o":
            getattr(WebSphere(), method)()
