
cluster = "{{cluster}}"
cell = "{{cell}}"
node = "{{node}}"
server = "{{server}}"
virtualHost = "{{virtualHost}}"
contextRoot = "{{contextRoot}}"

def listApplication():
    print AdminApp.list()

#-----------------------------------------------------------------
# Main
#-----------------------------------------------------------------

if __name__ == "__main__":
    listApplication()