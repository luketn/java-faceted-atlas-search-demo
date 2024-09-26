# Faceted MongoDB Atlas Full Text Search API Demo
This repository demonstrates how to build a Java API using MongoDB Atlas Search and its underlying Lucene indexing technology.

It shows off the awesome capabilities of MongoDB Atlas Search, including:
- Full text search
- Faceting to count results by category
- Filtering of results by category


### Sample Data
The sample data here includes links to images of dogs, their breeds colours and sizes as created by an LLM. 

An example document is:  
![image](https://github.com/user-attachments/assets/91ee1c60-0ac4-4b06-903b-6f9d789b858a)

```
{
  "url": "https://image-search.mycodefu.com/photos/Images/n02091134-whippet/n02091134_12142.jpg",
  "caption": "The image shows a brown and white dog running on a grassy field with a yellow frisbee in its mouth. The dog appears to be in mid-stride, with its front legs stretched out and its tail wagging. Its ears are perked up and its mouth is open, as if it is about to catch the Frisbee. The background is blurred, but it seems to be a sunny day with trees and a blue sky.",
  "summary": "A whippet dog is running across a grassy field with a yellow frisbee in its mouth.",
  "hasPerson": false,
  "dogs": [
    {
      "breed": "Whippet",
      "size": "Medium",
      "colour": [
        "Brown",
        "White"
      ]
    }
  ],
  "runData": {
    "filename": "photos/Images/n02091134-whippet/n02091134_12142.jpg",
    "captionTimeTakenSeconds": 0.86,
    "infoTimeTakenSeconds": 5
  },
  "breeds": [
    "Whippet"
  ],
  "colours": [
    "Brown",
    "White"
  ],
  "sizes": [
    "Medium"
  ]
}
```


You can start a docker container for Atlas with:
```bash
docker run -d --name mongodb-atlas -p 27017:27017 mongodb/mongodb-atlas-local
```

Then run the code to import the data with:
```bash
cd sample-data
./setup-local.sh
```

Due to the imperfect nature of LLMs there are a few incorrect records that can be cleaned up with:
```
db.photo.remove({colours: {$nin: ['Black', 'Brown', 'Golden', 'Grey', 'White']}});
db.photo.remove({colours: 'Afghan Hound'});
```

You can perform searches over the data like this:
```
db.photo.aggregate(
  [
    {
      "$search": {
        "index": "default",
        "facet": {
          "operator": {
            "compound": {
              "filter": [
                {
                  "text": {
                    "query": "frisbee",
                    "path": "summary"
                  }
                },
                {
                  "equals": {
                    "path": "colours",
                    "value": "White"
                  }
                }
              ]
            }
          },
          "facets": {
            "breeds": {
              "type": "string",
              "path": "breeds",
              "numBuckets": 10
            },
            "coloursFacet": {
              "type": "string",
              "path": "colours",
              "numBuckets": 10
            },
            "sizesFacet": {
              "type": "string",
              "path": "sizes",
              "numBuckets": 10
            }
          }
        }
      }
    },
    {
      $limit: 10
    },
    {
      "$facet": {
        docs: [],
        meta: [
          {
            "$replaceWith": "$$SEARCH_META"
          },
          {
            "$limit": 1
          }
        ]
      }
    }
  ]
)
```

We're using the 'text' operator of Atlas Search here, however there are several options. The differences in text search types are documented here:  
https://www.mongodb.com/resources/basics/full-text-search

