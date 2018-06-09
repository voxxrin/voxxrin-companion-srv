var items = [
    {key: 'LEARNED_SOMETHING', frLabel: 'J\'ai appris quelque chose'},
    {key: 'VERY_INTERESTING', frLabel: 'Hyper interessant'},
    {key: 'LOVE_DEMOS', frLabel: 'J\'aime les démos'},
    {key: 'NOT_SO_CLEAR', frLabel: 'Pas clair'},
    {key: 'NOT_SO_DYNAMIC', frLabel: 'Trop mou'},
    {key: 'NOT_SO_DEEP', frLabel: 'Pas assez profond'},
    {key: 'DID_NOT_UNDERSTAND', frLabel: 'J\'ai rien compris'},
    {key: 'LACKS_DEMO', frLabel: 'Ca manque de démos'},
    {key: 'TOO_FAST', frLabel: 'Trop rapide'},
    {key: 'TOO_COMPLICATED', frLabel: 'Trop compliqué'},
    {key: 'BEST_CONFERENCE_EVER', frLabel: 'Meilleure prez de ma vie !'},
    {key: 'FUN', frLabel: 'FUN !!'}
];

items.forEach(function (item) {
    item['@class'] = 'voxxrin.companion.domain.RatingItem';
    item.labels = {
        'FR': item.frLabel
    };
    delete item.frLabel;
    db.ratingItem.save(item);
});