(ns vignette.http.middleware-test
  (:require [midje.sweet :refer :all]
            [vignette.http.middleware :refer :all]))

(facts :add-cache-control-header
  (get (:headers (add-cache-control-header {})) "Cache-Control") => "public, max-age=1800"
  (get (:headers (add-cache-control-header nil)) "Cache-Control") => "public, max-age=1800"
  (get (:headers (add-cache-control-header {:status 500})) "Cache-Control") => "public, max-age=1800"
  (get (:headers (add-cache-control-header {:status 400})) "Cache-Control") => "public, max-age=3600"
  (get (:headers (add-cache-control-header {:status 401})) "Cache-Control") => "public, max-age=3600"
  (get (:headers (add-cache-control-header {:status 404})) "Cache-Control") => "public, max-age=3600"
  (get (:headers (add-cache-control-header {:status 200})) "Cache-Control") => "public, max-age=31536000"
  (get (:headers (add-cache-control-header {:status 201})) "Cache-Control") => "public, max-age=31536000")

(facts :multiple-slash->single-slash
       (uri-multiple-slash-replacement "/a/b/c/d") => "/a/b/c/d"
       (uri-multiple-slash-replacement "/a//b/c/d") => "/a/b/c/d"
       (uri-multiple-slash-replacement "//a///b////c/////d") => "/a/b/c/d")
