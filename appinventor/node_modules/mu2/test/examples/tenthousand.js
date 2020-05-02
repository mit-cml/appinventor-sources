(function () {
	var result = { foo: [] };

	for (var i = 0; i < 10000; i++) {
		result.foo.push({ value: i });
	}

	return result;
}())
