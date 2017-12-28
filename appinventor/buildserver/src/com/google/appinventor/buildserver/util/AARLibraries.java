// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.compiler.batch.BatchCompiler;

import com.android.builder.internal.SymbolLoader;
import com.android.builder.internal.SymbolWriter;
import com.android.ide.common.internal.PngCruncher;
import com.android.ide.common.res2.MergedResourceWriter;
import com.android.ide.common.res2.MergingException;
import com.android.ide.common.res2.ResourceMerger;
import com.android.ide.common.res2.ResourceSet;
import com.android.utils.ILogger;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * AARLibraries implements a set of {@link AARLibrary} and performs additional bookkeeping by
 * tracking the various classes, resources, assets, etc. packaged within each of the .aar files.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class AARLibraries extends HashSet<AARLibrary> {
  private static final long serialVersionUID = -5005733968228085856L;
  private static final ILogger LOG = new BaseLogger();

  /**
   * Absolute path to where generated source files will be written.
   */
  private final String generated;

  /**
   * The output directory where compiled class files will be written.
   */
  private File outputDir;

  /**
   * The classes.dex file present in the .aar files, if any.
   */
  private Set<File> classes = new HashSet<>();

  /**
   * The resources files present in the .aar files, if any.
   */
  private Set<File> resources = new HashSet<>();

  /**
   * The assets present in the .aar files, if any.
   */
  private Set<File> assets = new HashSet<>();

  /**
   * The libraries (.jar) files present in the .aar files, if any.
   */
  private Set<File> libraries = new HashSet<>();

  /**
   * The native libraries (.so) files present in the .aar files, if any.
   */
  private Set<File> natives = new HashSet<>();

  /**
   * Maps the package name for the dependency to any symbols it declares.
   */
  private Multimap<String, SymbolLoader> symbols = HashMultimap.create();

  /**
   * Construct a new AARLibraries collection.
   *
   * @param generated  directory where the generated, intermediate R.java files for each AAR
   *                   library will be written.
   */
  public AARLibraries(final File generated) {
    this.generated = generated.getAbsolutePath();
  }

  @Override
  public boolean add(AARLibrary e) {
    if (super.add(e)) {
      final String packageName = e.getPackageName();
      classes.add(e.getClassesJar());
      resources.addAll(e.getResources());
      assets.addAll(e.getAssets());
      libraries.addAll(e.getLibraries());
      natives.addAll(e.getNatives());
      try {
        if (e.getRTxt() != null) {
          SymbolLoader loader = new SymbolLoader(e.getRTxt(), LOG);
          loader.load();
          symbols.put(packageName, loader);
        }
      } catch(IOException ex) {
        throw new IllegalArgumentException("IOException merging resources", ex);
      }
      return true;
    }
    return false;
  }

  @Override
  public boolean remove(Object o) {
    // we don't support removing AAR libraries during compilation
    throw new UnsupportedOperationException();
  }

  public Set<File> getClasses() {
    return classes;
  }

  public Set<File> getResources() {
    return resources;
  }

  public Set<File> getAssets() {
    return assets;
  }

  public Set<File> getLibraries() {
    return libraries;
  }

  public Set<File> getNatives() {
    return natives;
  }

  public File getOutputDirectory() {
    return outputDir;
  }

  /**
   * Gets a list of resource sets loaded from the AAR libraries in the collection. Note that this
   * is computed on every call (results are not cached), so it is recommended that the caller only
   * call this after all AAR libraries of interest have been added.
   * @return  the list of all resource sets available across the AAR libraries.
   */
  private List<ResourceSet> getResourceSets() {
    List<ResourceSet> resourceSets = new ArrayList<>();
    for (AARLibrary library : this) {
      if (library.getResDirectory() != null) {
        ResourceSet resourceSet = new ResourceSet(library.getDirectory().getName());
        resourceSet.addSource(library.getResDirectory());
        resourceSets.add(resourceSet);
      }
    }
    return resourceSets;
  }

  /**
   * Merges the resources from all of the dependent AAR libraries into the main resource bundle for
   * the compiling app.
   *
   * @param outputDir the output directory to write the R.java files.
   * @param mainResDir the resource directory where the resource descriptors for the app reside.
   * @param cruncher configured PNG cruncher utility for reducing the size of PNG assets.
   * @return true if the merge was successful, otherwise false.
   */
  public boolean mergeResources(File outputDir, File mainResDir, PngCruncher cruncher) {
    List<ResourceSet> resourceSets = getResourceSets();
    ResourceSet mainResSet = new ResourceSet("main");
    mainResSet.addSource(mainResDir);
    resourceSets.add(mainResSet);
    ResourceMerger merger = new ResourceMerger();

    try {
      for (ResourceSet resourceSet : resourceSets) {
        resourceSet.loadFromFiles(LOG);
        merger.addDataSet(resourceSet);
      }

      MergedResourceWriter writer = new MergedResourceWriter(outputDir, cruncher, false, false, null);
      writer.setInsertSourceMarkers(true);
      merger.mergeData(writer, false);
      return true;
    } catch(MergingException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Writes out the R.class files for all of the ARR libraries as well as an R.class file for the
   * main app being compiled.
   *
   * @param outputDir the output directory to write the R.class files to.
   * @param appPackageName The package name of the currently compiling app.
   * @param appRTxt The app's R.txt file containing a list of resources.
   * @return 0 if the operation completes successfully, or 1 if an error occurs.
   * @throws IOException if the program is unable to read any R.txt files or write R.java or
   *                     R.class files
   * @throws InterruptedException if the compiler thread is interrupted.
   */
  public int writeRClasses(File outputDir, String appPackageName, File appRTxt) throws IOException, InterruptedException {
    this.outputDir = outputDir;
    SymbolLoader baseSymbolTable = new SymbolLoader(appRTxt, LOG);
    baseSymbolTable.load();

    // aggregate symbols into one writer per package
    Map<String, SymbolWriter> writers = new HashMap<>();
    for (String packageName : symbols.keys()) {
      Collection<SymbolLoader> loaders = symbols.get(packageName);
      SymbolWriter writer = new SymbolWriter(generated, packageName, baseSymbolTable);
      for (SymbolLoader loader : loaders) {
        writer.addSymbolsToWrite(loader);
      }
      writers.put(packageName, writer);
      writer.write();
    }

    // construct compiler command line
    List<String> args = new ArrayList<>();
    args.add("-1.7");
    args.add("-d");
    args.add(outputDir.getAbsolutePath());
    args.add(generated);

    // compile R classes using ECJ batch compiler
    PrintWriter out = new PrintWriter(System.out);
    PrintWriter err = new PrintWriter(System.err);
    if (BatchCompiler.compile(args.toArray(new String[0]), out, err, new NOPCompilationProgress())) {
      return 0;
    } else {
      return 1;
    }
  }
}
