import sys
import getopt

class WAS:
    def listApplications(self):
        print "list applications"
        print AdminApp.list()

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

    def stopApplication(self):
        print "stopping application"
        appManager = AdminControl.queryNames('node={{node}},type=ApplicationManager,process={{server}},*')
        print appManager
        AdminControl.invoke(appManager, 'stopApplication', '{{applicationName}}')

    def installApplication(self):
        print "installing application"
        pass

    def uninstallApplication(self):
        print "uninstalling application"
        pass

    def isApplicationInstalled(self):
        return AdminApplication.checkIfAppExists('{{applicationName}}')


#-----------------------------------------------------------------
# Main
#-----------------------------------------------------------------

if __name__ == "__main__":
    methods, args = getopt.getopt(sys.argv, 'o:')
    for name, method in methods:
        if name == "-o":
            getattr(WAS(), method)()
