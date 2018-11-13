var eventId = 'bdxio18';
var results = [];
var items = db.ratingItem.find().toArray();

print(db.rating.distinct('userId', {presentationRef: {$regex: '^' + eventId}}).length + ' users');

db.rating.aggregate([
    {$match: {presentationRef: {$regex: '^' + eventId}}},
    {$unwind: '$ratingItems'},
    {
        $group: {
            _id: '$presentationRef',
            items: {
                $push: '$ratingItems.key'
            }
        }
    },
    {
        $addFields: {
            externalId: {$substr: ['$_id', 8, -1]}
        }
    },
    {
        $lookup: {
            from: 'presentation',
            localField: 'externalId',
            foreignField: 'externalId',
            as: 'prez'
        }
    },
    {
        $unwind: '$prez'
    },
    {$sort: {'prez.from': 1}},
    {
        $project: {
            _id: 0,
            externalId: '$externalId',
            title: '$prez.title',
            speakers: '$prez.speakers',
            items: '$items'
        }
    }
]).forEach(function (result) {

    result.occurences = {};
    items.forEach(item => {
        const occurences = result.items.filter(anItem => item.key === anItem).length;
        if (occurences > 0) {
            result.occurences[item.labels.FR] = occurences;
        }
    });
    delete result.items;

    result.speakers = result.speakers
        .map(speakerRef => speakerRef.replace('ref://speaker/', ''))
        .map(speakerId => ObjectId(speakerId))
        .map(speakerId => db.speaker.findOne(speakerId))
        .map(speaker => speaker.name);

    results.push(result);
});

printjson(results);
