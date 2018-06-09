var collections = ['room', 'event', 'ratingItem', 'presentation', 'day', 'speaker'];
var exec = true;

collections.forEach(function (collection) {
    var firstDocument = db[collection].findOne();
    if (!firstDocument) {
        print('empty collection ' + collection);
    } else {
        var clazz = firstDocument['@class'];
        var newClazz = clazz.replace('voxxrin2', 'voxxrin.companion');
        print(clazz + ' -> ' + newClazz);
        if (exec === true) {
            db[collection].update({}, {$set: {'@class': newClazz}}, {multi: true});
        }
    }
});