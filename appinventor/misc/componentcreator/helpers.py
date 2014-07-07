import re
from shutil import copy

class DuplicateError(Exception):
    pass

class NewComponent():

    def __init__(self, compName=None, compImgName=None):
        self.compName = compName
        self.compImgName = compImgName
        self.imgFile = None

    def addImageReference(self):
        # read original file
        with open('../../appengine/src/com/google/appinventor/client/Images.java', 'r') as f:
            text = f.read()

        #open template and insert variables
        with open('Image.java.template') as f:
            newtxt = f.read() % (
                self.compName, self.compImgName, self.compImgName)

        # if already exists a declaration with same name
        if re.search(re.escape(newtxt), text):
            raise DuplicateError

        # insert new component in file and write
        text = re.sub(re.escape('procedures();'), 'procedures();\n%s' % newtxt, text)

        with open('../../appengine/src/com/google/appinventor/client/Images.java', 'w') as f:
            f.write(text)

    def copyImageToFolder(self):
        # if user enabled resizing
        if self.resizeImage:
            from PIL import Image

            # crop and resize image
            im = Image.open(self.imgFile)
            diff = max(im.size) - min(im.size)
            if im.size[0] > im.size[1]:
                box = (diff / 2, 0, im.size[0] - diff / 2, im.size[1])
            else:
                box = (0, diff / 2, im.size[0], im.size[1] - diff / 2)

            im = im.crop(box)
            im.thumbnail((16, 16), Image.ANTIALIAS)

            im.save('../../appengine/src/com/google/appinventor/images/%s.png' %
                self.compImgName, "PNG")

        else:
            copy(self.imgFile, '../../appengine/src/com/google/appinventor/images/%s.png' % self.compImgName)


    def createMockComponent(self):
        if self.visibleComponent:
            # if visible component, create new mockcomponent
            with open('Mock.java.template') as f:
                mockFileContent = f.read(
                ) % (self.compName, self.compName, self.compName, self.compName,
                     self.compName, self.compName, self.compImgName, self.compName)
                with open(
                    '../../appengine/src/com/google/appinventor/client/editor/simple/components/Mock%s.java' %
                    self.compName, 'w') as f:
                    f.write(mockFileContent)

        # update simple component descriptor with new component
        with open('../../appengine/src/com/google/appinventor/client/editor/simple/palette/SimpleComponentDescriptor.java', 'r') as f:
            text = f.read()

            if self.visibleComponent:
                basetxt = 'import com.google.appinventor.client.editor.simple.components.'
                newtxt =  basetxt + 'Mock' + self.compName + ';'
                text = re.sub(re.escape(basetxt + 'MockWebViewer;'),
                                basetxt + 'MockWebViewer;\n' + newtxt, text)

                basetxt = 'return new MockWebViewer(editor);'
                newtxt =  '    } else if (name.equals(Mock%s.TYPE)) {\n      return new Mock%s(editor);' % (self.compName, self.compName)
                text = re.sub(re.escape(basetxt), basetxt + '\n'+newtxt, text)


            basetxt = 'bundledImages.put("images/web.png", images.web());'
            text = re.sub(re.escape(basetxt), basetxt + '\n    bundledImages.put("images/%s.png", images.%s());'
                          % (self.compImgName, self.compImgName), text)

        with open('../../appengine/src/com/google/appinventor/client/editor/simple/palette/SimpleComponentDescriptor.java', 'w') as f:
            f.write(text)

    def createComponent(self):
        version = self.compName.upper() + '_COMPONENT_VERSION'

        # get template for visible or nonvisible component and write to file
        if self.visibleComponent:
            with open('VisibleComponent.java.template') as f:
                content = f.read() % (
                    version, self.category, self.compImgName, self.compName, self.compName, self.compName)
                with open('../../components/src/com/google/appinventor/components/runtime/%s.java' % self.compName, 'w') as f2:
                    f2.write(content)
        else:
            with open('NonVisibleComponent.java.template') as f:
                content = f.read() % (
                    version, self.category, self.compImgName, self.compName, self.compName)
                with open('../../components/src/com/google/appinventor/components/runtime/%s.java' % self.compName, 'w') as f2:
                    f2.write(content)


        with open('../../components/src/com/google/appinventor/components/common/YaVersion.java', 'r') as f:
            text = f.read()
            oldtxt = 'public static final int SLIDER_COMPONENT_VERSION = 1;'
            newtxt = '\n\n\n  // For %s 1:\n  // - Initial Version.\n' % version
            newtxt += '  public static final int %s = 1;' % version
            text = re.sub(re.escape(oldtxt), oldtxt + newtxt, text)

        with open('../../components/src/com/google/appinventor/components/common/YaVersion.java', 'w') as f:
            f.write(text)
