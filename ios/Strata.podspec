require 'json'

package = JSON.parse(File.read(File.join(__dir__, '..', 'package.json')))

Pod::Spec.new do |s|
  s.name         = 'Strata'
  s.version      = package['version']
  s.summary      = package['description']
  s.homepage     = package['homepage']
  s.license      = package['license']
  s.author       = package['author']
  s.source       = { :git => package['repository']['url'], :tag => s.version }
  s.source_files = '*.{h,m,swift}'
  s.platform     = :ios, '14.0'
  s.swift_version = '5.0'
  
  s.dependency 'React-Core'
end
