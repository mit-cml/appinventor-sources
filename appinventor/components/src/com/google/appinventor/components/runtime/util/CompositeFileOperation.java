package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.common.FileScope;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.Form;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class CompositeFileOperation extends FileOperation
    implements Iterable<CompositeFileOperation.FileOperand> {

  public static class FileOperand {
    private final String file;
    private final FileAccessMode mode;

    FileOperand(String file, FileAccessMode mode) {
      this.file = file;
      this.mode = mode;
    }

    public String getFile() {
      return file;
    }

    public FileAccessMode getMode() {
      return mode;
    }
  }

  private final List<FileOperand> files = new ArrayList<>();
  private final Set<String> permissions = new HashSet<>();
  private boolean needsExternalStorage = false;

  public CompositeFileOperation(Form form, Component component, String method, boolean async) {
    super(form, component, method, async);
  }

  public void addFile(FileScope scope, String fileName, FileAccessMode mode) {
    FileOperand operand = new FileOperand(FileUtil.resolveFileName(form, fileName, scope), mode);
    files.add(operand);
    permissions.add(FileUtil.getNeededPermission(form, fileName, mode));
    needsExternalStorage |= FileUtil.isExternalStorageUri(form, operand.file);
  }

  @Override
  public List<String> getPermissions() {
    return new ArrayList<>(permissions);
  }

  @Override
  protected abstract void performOperation();

  @Override
  protected boolean needsExternalStorage() {
    return needsExternalStorage;
  }

  @Override
  public Iterator<FileOperand> iterator() {
    return files.iterator();
  }
}
