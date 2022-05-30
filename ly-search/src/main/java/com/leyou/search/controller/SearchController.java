package com.leyou.search.controller;

import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.dto.SearchResult;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 搜索
 */
@RestController
public class SearchController {
    @Autowired
    private SearchService searchService;

    /**
     * 商品搜索
     */
    @PostMapping("/page")
    public ResponseEntity<SearchResult<GoodsDTO>> goodsSearchPage(@RequestBody SearchRequest searchRequest){
        SearchResult<GoodsDTO> searchResult = searchService.goodsSearchPage(searchRequest);
        return ResponseEntity.ok(searchResult);
    }

    /**
     * 商品换页
     */
    @PostMapping("/page/change")
    public ResponseEntity<List<GoodsDTO>> goodsSearchPageChange(@RequestBody SearchRequest searchRequest){
        List<GoodsDTO> searchResult = searchService.goodsSearchPageChange(searchRequest);
        return ResponseEntity.ok(searchResult);
    }
}
