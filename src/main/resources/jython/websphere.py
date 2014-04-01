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
packageFile = r"{{packageFile}}"


class WebSphere:
    def listApplications(self):
        print "[LIST APPLICATIONS]", host
        print AdminApp.list()

    def restartServer(self):
        print '-'*60
        print "[RESTARTING SERVER]", host
        print '-'*60
        if "" != cluster:
            print AdminTask.updateAppOnCluster('[-ApplicationNames ' + applicationName + ' -timeout 600]')
        else:
            try:
                appManager = AdminControl.queryNames('node=HaoNode01,type=ApplicationManager,process=server1,*')
                print AdminControl.invoke(appManager, 'restart')
            except:
                print "Failed to restart server:"
                print '-'*10
                traceback.print_exc(file=sys.stdout)
                print '-'*10
                print "try to startApplication directly..."
                self.startApplication()

    def startApplication(self):
        print '-'*60
        print "[STARTING APPLICATION]", host, applicationName
        print '-'*60
        if "" == node:
            appManager = AdminControl.queryNames('type=ApplicationManager,process=' + server + ',*')
        else:
            appManager = AdminControl.queryNames('node=' + node + ',type=ApplicationManager,process=' + server + ',*')
        print AdminControl.invoke(appManager, 'startApplication', applicationName)
        #AdminApplication.startApplicationOnCluster(applicationName, cluster)

    def stopApplication(self):
        print '-'*60
        print "[STOPPING APPLICATION]", host, applicationName
        print '-'*60
        if "" == node:
            appManager = AdminControl.queryNames('type=ApplicationManager,process=' + server + ',*')
        else:
            appManager = AdminControl.queryNames('node=' + node + ',type=ApplicationManager,process=' + server + ',*')
        print AdminControl.invoke(appManager, 'stopApplication', applicationName)
        #AdminApplication.stopApplicationOnCluster(applicationName, cluster)

    def installApplication(self):
        try:
            print '-'*60
            print "[INSTALLING APPLICATION]", host, applicationName
            print '-'*60
            if "" != cluster:
                serverMapping = 'WebSphere:cluster=' + cluster
                options = ['-deployws', '-distributeApp', '-appname', applicationName, '-cluster', cluster, '-server', server, '-MapModulesToServers', [['.*','.*', serverMapping]], '-MapWebModToVH', [['.*','.*', virtualHost]]]
            elif "" != contextRoot:
                serverMapping = 'WebSphere:server=' + server
                options = ['-distributeApp', '-appname', applicationName, '-contextroot', contextRoot, '-server', server, '-MapModulesToServers', [['.*','.*', serverMapping]], '-MapWebModToVH', [['.*','.*', virtualHost]]]
            else:
                serverMapping = 'WebSphere:server=' + server
                options = ['-distributeApp', '-appname', applicationName, '-server', server, '-MapModulesToServers', [['.*','.*', serverMapping]], '-MapWebModToVH', [['.*','.*', virtualHost]]]

            print "INSTALLING"
            print AdminApp.install(packageFile, options)

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
            return "true"
        except:
            print "Failed to install application: ", applicationName
            print '-'*10
            traceback.print_exc(file=sys.stdout)
            print '-'*10
            return "false"

    def uninstallApplication(self):
        print '-'*60
        print "[UNINSTALLING APPLICATION]", host, applicationName
        print '-'*60
        print AdminApp.uninstall(applicationName)
        AdminConfig.save()
        if "" != cluster:
            AdminNodeManagement.syncActiveNodes()

    def isApplicationInstalled(self):
        return AdminApplication.checkIfAppExists(applicationName)

    def deploy(self):
        if "true" == self.isApplicationInstalled():
            self.uninstallApplication()

        if "true" == self.installApplication():
            self.restartServer()


#-----------------------------------------------------------------
# Main
#-----------------------------------------------------------------

if __name__ == "__main__":
    methods, args = getopt.getopt(sys.argv, 'o:')
    for name, method in methods:
        if name == "-o":
            getattr(WebSphere(), method)()
