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
        print time.strftime("%Y/%b/%d %H:%M:%S %Z")
        print AdminApp.list()

    def restartServer(self):
        print '-'*60
        print "[RESTARTING SERVER]", host
        print time.strftime("%Y/%b/%d %H:%M:%S %Z")
        print '-'*60
        if "" != cluster:
            try:
                appManager = AdminControl.queryNames('name=' + cluster + ',type=Cluster,process=dmgr,*')
                print AdminControl.invoke(appManager, 'rippleStart')
            except:
                print "Failed to restart cluster:"
                print '-'*10
                traceback.print_exc(file=sys.stdout)
                print '-'*10
                print "try to startApplication directly..."
                self.startApplication()
        else:
            try:
                appManager = AdminControl.queryNames('node=' + node + ',type=ApplicationManager,process=' + server + ',*')
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
        print time.strftime("%Y/%b/%d %H:%M:%S %Z")
        print '-'*60
        try:
            if "" == node:
                appManager = AdminControl.queryNames('type=ApplicationManager,process=' + server + ',*')
            else:
                appManager = AdminControl.queryNames('node=' + node + ',type=ApplicationManager,process=' + server + ',*')
            print AdminControl.invoke(appManager, 'startApplication', applicationName)
            #AdminApplication.startApplicationOnCluster(applicationName, cluster)
        except:
            print "Failed to start application:"
            print '-'*10
            traceback.print_exc(file=sys.stdout)
            print '-'*10

    def stopApplication(self):
        print '-'*60
        print "[STOPPING APPLICATION]", host, applicationName
        print time.strftime("%Y/%b/%d %H:%M:%S %Z")
        print '-'*60
        try:
            if "" == node:
                appManager = AdminControl.queryNames('type=ApplicationManager,process=' + server + ',*')
            else:
                appManager = AdminControl.queryNames('node=' + node + ',type=ApplicationManager,process=' + server + ',*')
            print AdminControl.invoke(appManager, 'stopApplication', applicationName)
            #AdminApplication.stopApplicationOnCluster(applicationName, cluster)
        except:
            print "Failed to stop application:"
            print '-'*10
            traceback.print_exc(file=sys.stdout)
            print '-'*10

    def installApplication(self):
        print '-'*60
        print "[INSTALLING APPLICATION]", host, applicationName
        print time.strftime("%Y/%b/%d %H:%M:%S %Z")
        print '-'*60
        try:
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
        print time.strftime("%Y/%b/%d %H:%M:%S %Z")
        print '-'*60
        try:
            print AdminApp.uninstall(applicationName)
            AdminConfig.save()
            if "" != cluster:
                AdminNodeManagement.syncActiveNodes()
        except:
            print "Failed to uninstall application: ", applicationName
            print '-'*10
            traceback.print_exc(file=sys.stdout)
            print '-'*10

    def isApplicationInstalled(self):
        return AdminApplication.checkIfAppExists(applicationName)

    def deploy(self):
        if "true" == self.isApplicationInstalled():
            self.uninstallApplication()

        if "true" == self.installApplication():
            self.restartServer()

        print '-'*60
        print "[FINISHED]", host, applicationName
        print time.strftime("%Y/%b/%d %H:%M:%S %Z")
        print '-'*60


#-----------------------------------------------------------------
# Main
#-----------------------------------------------------------------

if __name__ == "__main__":
    methods, args = getopt.getopt(sys.argv, 'o:')
    for name, method in methods:
        if name == "-o":
            getattr(WebSphere(), method)()
