import sys
import getopt
import time


class WAS:
    def listApplications(self):
        print "list applications"
        print AdminApp.list()
        return AdminApplication.listApplications()

    def restartServer(self):
        print "restarting server"
        if "" != {{cluster}}:
            print AdminTask.updateAppOnCluster('[-ApplicationNames {{applicationName}} -timeout 3600]')
        else:
            print AdminControl.stopServer('{{server}}', '{{node}}')
            print AdminControl.startServer('{{server}}', '{{node}}')

    def startApplication(self):
        print "starting application"
        appManager = AdminControl.queryNames('node={{node}},type=ApplicationManager,process={{server}},*')
        print appManager
        AdminControl.invoke(appManager, 'startApplication', '{{applicationName}}')
        #AdminApplication.startApplicationOnCluster("{{applicationName}}", "{{cluster}}")

    def stopApplication(self):
        print "stopping application"
        appManager = AdminControl.queryNames('node={{node}},type=ApplicationManager,process={{server}},*')
        print appManager
        AdminControl.invoke(appManager, 'stopApplication', '{{applicationName}}')
        #AdminApplication.stopApplicationOnCluster("{{applicationName}}", "{{cluster}}")

    def installApplication(self):
        print "installing application: {{applicationName}}"
        serverMapping = 'WebSphere:cluster={{cluster}}'
        if "" != {{contextRoot}}:
            options = ['-distributeApp', '-appname', {{applicationName}}, '-contextroot', '{{contextRoot}}', '-cluster', {{cluster}}, '-server', {{server}}, '-MapModulesToServers', [['.*','.*', serverMapping]], '-MapWebModToVH', [['.*','.*', {{virtualHost}}]]]
        else:
            options = ['-deployws', '-distributeApp', '-appname', {{applicationName}}, '-cluster', {{cluster}}, '-server', {{server}}, '-MapModulesToServers', [['.*','.*', serverMapping]], '-MapWebModToVH', [['.*','.*', {{virtualHost}}]]]
        AdminApp.install('{{packageFile}}', options)
        AdminConfig.save()
        AdminNodeManagement.syncActiveNodes()

        result = AdminApp.isAppReady('{{applicationName}}')
        while result == "false":
            print AdminApp.getDeployStatus('{{applicationName}}')
            time.sleep(5)
            result = AdminApp.isAppReady('{{applicationName}}')
        print "{{applicationName}} installed"

    def uninstallApplication(self):
        print "uninstalling application: {{applicationName}}"
        AdminApp.uninstall('{{applicationName}}')
        AdminConfig.save()
        AdminNodeManagement.syncActiveNodes()

    def isApplicationInstalled(self):
        return AdminApplication.checkIfAppExists('{{applicationName}}')

    def deploy(self):
        if "true" == isApplicationInstalled():
            uninstallApplication()

        installApplication()
        restartServer()


#-----------------------------------------------------------------
# Main
#-----------------------------------------------------------------

if __name__ == "__main__":
    methods, args = getopt.getopt(sys.argv, 'o:')
    for name, method in methods:
        if name == "-o":
            getattr(WAS(), method)()
