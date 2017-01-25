package com.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.integration.file.filters.FileListFilter;
import org.springframework.util.Assert;

public class ChainFileListFilter<F> implements FileListFilter<F> {

	private final List<FileListFilter<F>> fileFilters = new ArrayList<>();

	public ChainFileListFilter<F> addFilter(FileListFilter<F> filter) {
		fileFilters.add(filter);
		return this;
	}

	@Override
	public List<F> filterFiles(F[] files) {
		Assert.notNull(files, "'files' should not be null");
		List<F> leftOver = Arrays.asList(files);
		for (FileListFilter<F> fileFilter : this.fileFilters) {
			@SuppressWarnings("unchecked")
			F[] fileArray = (F[]) leftOver.toArray();
			leftOver = fileFilter.filterFiles(fileArray);
		}
		return leftOver;
	}

}
