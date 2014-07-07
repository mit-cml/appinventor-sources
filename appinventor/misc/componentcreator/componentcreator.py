from Tkinter import Tk, BOTH, END, IntVar, StringVar, LEFT
from ttk import Button, Checkbutton, Frame, Style, Combobox, Label, Entry
from tkFileDialog import askopenfilename
import tkMessageBox

from helpers import *

import os
abspath = os.path.abspath(__file__)
dname = os.path.dirname(abspath)
os.chdir(dname)



class Application(Frame):
    def __init__(self, master=None):
        Frame.__init__(self, master)
        self.pack(fill=BOTH, expand=1)
        self.initUI()
        self.setGeometry()
        self.component = NewComponent()

    def setGeometry(self):
        x = 300
        y = 100
        self.master.geometry("400x300+%d+%d" % (x, y))
        self.master.update()


    def initUI(self):
        #setup title
        self.master.title("Component Creator")
        self.style = Style()
        self.style.theme_use("clam")

        #indicator label
        self.labelName = Label(self, text="Component Name:")
        self.labelName.place(x=10, y=10)
        self.master.update()

        # create variable and namefield for input of component name
        sv = StringVar()
        sv.trace("w", lambda name, index, mode, sv=sv: self.nameChanged(sv))
        self.nameField = Entry(self, textvariable=sv)
        self.nameField.place(x=10+self.labelName.winfo_width() + 10, y=10)
        self.master.update()

        # label for image name that will show img name for a given component name
        self.imgNameVar = StringVar()
        self.imgNameVar.set('imageName:')
        self.labelImageName = Label(self, textvariable=self.imgNameVar)
        self.labelImageName.place(x=10+self.labelName.winfo_width()+10,y=40)

        # checkbox for visible component or not
        self.cbVar = IntVar()
        self.cb = Checkbutton(self, text="Visible Component",  variable=self.cbVar)
        self.cb.place(x=10, y=70)

        # dropdown list for category
        self.labelCategory = Label(self, text="Category:")
        self.labelCategory.place(x=10, y=110)
        self.master.update()

        acts = ['UserInterface', 'Layout', 'Media', 'Animation', 'Sensors', 'Social', 'Storage',
                'Connectivity', 'LegoMindStorms', 'Experimental', 'Internal', 'Uninitialized']

        self.catBox = Combobox(self, values=acts)
        self.catBox.place(x=10+self.labelCategory.winfo_width()+10, y=110)

        # button to select icon image
        self.getImageButton = Button(self, text="Select icon", command=self.getImage)
        self.getImageButton.place(x=10, y=150)
        self.master.update()

        # explanation for resizing
        self.resizeVar = IntVar()
        self.resizeCB = Checkbutton(self,
            text="ON=Resize Image (Requires PIL)\nOFF=Provide 16x16 Image", variable=self.resizeVar)
        self.resizeCB.place(x=10+self.getImageButton.winfo_width()+10, y=150)

        # create button
        self.createButton = Button(self, text="Create", command=self.create)
        self.createButton.place(x=10, y=230)

        #cancel button
        self.cancelButton = Button(self, text="Cancel", command=self.quit)
        self.cancelButton.place(x=200, y=230)


    # open file picker for selecting an icon
    def getImage(self):
        ftypes = [('All Picture Files', ('*.jpg', '*.png', '*.jpeg', '*.bmp')), ('All files', '*')]
        self.component.imgFile = askopenfilename(filetypes=ftypes, title="Select an Icon file")

    # update component name and image name for component by lowercasing first letter
    def nameChanged(self, sv):
        s = sv.get()
        self.component.compName = s
        self.component.compImgName = s[:1].lower() + s[1:] if s else ''
        self.imgNameVar.set('imageName: %s' % self.component.compImgName)

    # tries to create component
    def create(self):
        # sets parameters for new component based on input values
        self.component.visibleComponent = bool(self.cbVar.get())
        self.component.resizeImage = bool(self.resizeVar.get())
        self.component.category = self.catBox.get().upper()
        self.component.compName = self.nameField.get()

        try:
            # check if component already exists
            try:
                open('../../components/src/com/google/appinentor/components/runtime/%s.java', 'r')
                tkMessageBox.showerror("Duplicate Component","%s already exists" % self.component.compName)
            # if doesnt exist will raise error
            except IOError:
                # check for name input
                if not self.component.compImgName:
                    tkMessageBox.showerror("Missing Name","Please enter component name")
                    return

                #check for category selection
                if not self.component.category:
                    tkMessageBox.showerror("Missing Category","Please select a category")
                    return

                # check if selected an icon
                if not self.component.imgFile:
                    tkMessageBox.showerror("Missing Icon","Please select an icon image")
                    return

                # copy image file to folder, can get error if user checked resize and doest have PIL installed
                try:
                    self.component.copyImageToFolder()
                except ImportError, e:
                    tkMessageBox.showerror("Unable to import PIL","Please install PIL or unselect checkbox")
                    return

                # add references to the image file, can get error if component already exists
                try:
                    self.component.addImageReference()
                except DuplicateError, e:
                    tkMessageBox.showerror("Duplicate Component","%s already exists" % self.component.compName)
                    return

                # will create mock component if is visible and add references to SimpleComponentDescriptor
                self.component.createMockComponent()

                # will create the actual component file
                self.component.createComponent()

                tkMessageBox.showinfo('Success', 'Component created successfully')

        # if could not open some file for writing
        except Exception, e:
            tkMessageBox.showerror("Exception",str(e))


def main():
    root = Tk()

    # sets window size and geometry
    app = Application(master=root)
    root.mainloop()


if __name__ == '__main__':
    main()



