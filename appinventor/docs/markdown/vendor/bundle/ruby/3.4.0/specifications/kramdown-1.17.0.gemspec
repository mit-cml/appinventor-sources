# -*- encoding: utf-8 -*-
# stub: kramdown 1.17.0 ruby lib

Gem::Specification.new do |s|
  s.name = "kramdown".freeze
  s.version = "1.17.0".freeze

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.require_paths = ["lib".freeze]
  s.authors = ["Thomas Leitner".freeze]
  s.date = "2018-05-31"
  s.description = "kramdown is yet-another-markdown-parser but fast, pure Ruby,\nusing a strict syntax definition and supporting several common extensions.\n".freeze
  s.email = "t_leitner@gmx.at".freeze
  s.executables = ["kramdown".freeze]
  s.files = ["bin/kramdown".freeze]
  s.homepage = "http://kramdown.gettalong.org".freeze
  s.licenses = ["MIT".freeze]
  s.rdoc_options = ["--main".freeze, "lib/kramdown/document.rb".freeze]
  s.required_ruby_version = Gem::Requirement.new(">= 2.0".freeze)
  s.rubygems_version = "2.7.3".freeze
  s.summary = "kramdown is a fast, pure-Ruby Markdown-superset converter.".freeze

  s.installed_by_version = "3.6.9".freeze

  s.specification_version = 4

  s.add_development_dependency(%q<minitest>.freeze, ["~> 5.0".freeze])
  s.add_development_dependency(%q<coderay>.freeze, ["~> 1.0.0".freeze])
  s.add_development_dependency(%q<rouge>.freeze, [">= 0".freeze])
  s.add_development_dependency(%q<stringex>.freeze, ["~> 1.5.1".freeze])
  s.add_development_dependency(%q<prawn>.freeze, ["~> 2.0".freeze])
  s.add_development_dependency(%q<prawn-table>.freeze, ["~> 0.2.2".freeze])
  s.add_development_dependency(%q<ritex>.freeze, ["~> 1.0".freeze])
  s.add_development_dependency(%q<itextomml>.freeze, ["~> 1.5".freeze])
  s.add_development_dependency(%q<execjs>.freeze, ["~> 2.7".freeze])
  s.add_development_dependency(%q<sskatex>.freeze, [">= 0.9.37".freeze])
  s.add_development_dependency(%q<katex>.freeze, ["~> 0.4.3".freeze])
end
