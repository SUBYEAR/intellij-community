package com.intellij.openapi.vcs.impl;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;

/**
 * @author yole
 */
public class ModuleVcsPathPresenter extends VcsPathPresenter {
  private Project myProject;

  public ModuleVcsPathPresenter(final Project project) {
    myProject = project;
  }

  public String getPresentableRelativePathFor(final VirtualFile file) {
    if (file == null) return "";
    return ApplicationManager.getApplication().runReadAction(new Computable<String>() {
      public String compute() {
        ProjectFileIndex fileIndex = ProjectRootManager.getInstance(myProject)
          .getFileIndex();
        Module module = fileIndex.getModuleForFile(file);
        VirtualFile contentRoot = fileIndex.getContentRootForFile(file);
        if (module == null || contentRoot == null) return file.getPresentableUrl();
        StringBuffer result = new StringBuffer();
        result.append("[");
        result.append(module.getName());
        result.append("] ");
        result.append(contentRoot.getName());
        String relativePath = VfsUtil.getRelativePath(file, contentRoot, File.separatorChar);
        if (relativePath.length() > 0) {
          result.append(File.separatorChar);
          result.append(relativePath);
        }
        return result.toString();
      }
    });
  }

  public String getPresentableRelativePath(final ContentRevision fromRevision, final ContentRevision toRevision) {
    // need to use parent path because the old file is already not there
    final VirtualFile oldFile = fromRevision.getFile().getParentPath().getVirtualFile();
    final VirtualFile newFile = toRevision.getFile().getParentPath().getVirtualFile();
    if (oldFile != null && newFile != null) {
      Module oldModule = ModuleUtil.findModuleForFile(oldFile, myProject);
      Module newModule = ModuleUtil.findModuleForFile(newFile, myProject);
      if (oldModule != newModule) {
        return getPresentableRelativePathFor(oldFile);
      }
    }
    return FileUtil.getRelativePath(toRevision.getFile().getIOFile(), fromRevision.getFile().getIOFile());
  }

}