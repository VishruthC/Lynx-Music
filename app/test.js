fetch('https://open.spotify.com/oembed?url=https://open.spotify.com/playlist/37i9dQZF1DXcBWIGoYBM5M')
  .then(res => res.json())
  .then(json => console.log(JSON.stringify(json, null, 2)))
  .catch(err => console.error(err));
