function parse_artifact_uri(uri) {

    var re = /^.*\/org\/arachne-framework\/([\w-]+)\/(.*)\/.*$/
    var vre = /(\d)+\.(\d)+\.(\d)+((-.*-)(\d{4})-)*/

    var result = re.exec(uri);
    var version_str = result[2];

    var pv = vre.exec(version_str);
    var version={major: parseInt(pv[1]),
                 minor: parseInt(pv[2]),
                 patch: parseInt(pv[3]),
                 commit: parseInt(pv[6]) || 0};

    return {version: version, version_str: version_str};
}

function most_recent_version(versions) {
    return _.last(_.sortBy(versions, function (artifact) {
        var version = artifact.version;
        return (version.major * 100000000) +
               (version.minor * 1000000) +
               (version.patch * 10000) +
            version.commit;
    }));
}

// Find the latest version of the specified artifact, and replace the text contents of the specified selector with the actual version
function version_of(selector, name) {
    var url = "http://maven.arachne-framework.org/artifactory/api/search/artifact?name=" + name;

    $.ajax(url, {success: onSuccess, error: onError});

   $(selector).text(function(i,txt){
       var original = "<" + name + "-version>";
       return txt.replace(original, "loading...")
    });

    function onSuccess(data) {
        var uris = _.map(data.results, function(d) { return d.uri; })
        var versions = _.map(uris,parse_artifact_uri);
        if (_.isEmpty(versions)) {
           onError();
           return;
        }

        var version = most_recent_version(versions).version_str;
        $(selector).text(function(i,txt){
            return txt.replace("loading...", version)
        });
    }

    function onError() {
        $(selector).text("<<API ERROR: maven.arachne-framework.org>>")
    }

}

function lookup_versions() {
    $(".version-lookup").each(function (i, el) {
        var name = $(el).attr("data-artifact");
        version_of(el, name);
    });
}

var version_re = /^\"?<([\w-]+)-version>\"?$/
function replace_versions() {
     $(document).find("code span,code").filter(function () {
        var match = version_re.exec(this.textContent);
        if (match) {
          return true;
        } else {
          return false;
        }

    }).each(function(){
       version_of(this, version_re.exec($(this).text())[1]);
    });
}

$( window ).on( "load", function() {
    replace_versions();
});